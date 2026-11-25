package group.backgroundgremlin.clankcon;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;
import java.nio.charset.CodingErrorAction;
import java.security.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;

final class Vault {
    static final int TEXT_LIMIT=16*1024*1024;
    static final String ALIAS="clank-con-vault-v1";
    final File dir; final SecretKey key; private List<Record> dedupIndex;
    Vault(Context c)throws Exception {
        dir=new File(c.getFilesDir(),"vault"); if(!dir.isDirectory()&&!dir.mkdirs())throw new IOException("Cannot create vault");
        KeyStore ks=KeyStore.getInstance("AndroidKeyStore");ks.load(null);
        if(!ks.containsAlias(ALIAS)){KeyGenerator kg=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");kg.init(new KeyGenParameterSpec.Builder(ALIAS,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setKeySize(256).build());kg.generateKey();}
        key=(SecretKey)ks.getKey(ALIAS,null);
    }
    Vault(File directory,SecretKey k)throws IOException{dir=directory;key=k;if(!dir.isDirectory()&&!dir.mkdirs())throw new IOException("Cannot create restore staging vault");}
    static final class Record {
        final JSONObject meta; final File dir;
        Record(JSONObject m,File d){meta=m;dir=d;}
        String id(){return meta.optString("id");} String title(){return meta.optString("title");} String provider(){return meta.optString("provider");} String account(){return meta.optString("account");} String kind(){return meta.optString("mime");}
    }
    static void copy(InputStream in,OutputStream out)throws IOException{byte[] b=new byte[65536];for(int n;(n=in.read(b))!=-1;)out.write(b,0,n);}
    static byte[] bounded(InputStream in,int max)throws IOException{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[16384];int total=0;for(int n;(n=in.read(b))!=-1;){if(n>max-total)throw new IOException("Content exceeds text processing limit of "+max+" bytes. Original can still be preserved and exported as ZIP.");out.write(b,0,n);total+=n;}return out.toByteArray();}
    static String hex(byte[] bytes){StringBuilder s=new StringBuilder();for(byte b:bytes)s.append(String.format(Locale.ROOT,"%02x",b&255));return s.toString();}
    static byte[] utf8(String s){return s.getBytes(StandardCharsets.UTF_8);}
    static String decode(byte[] b)throws Exception{return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(b)).toString();}
    OutputStream encrypted(File f,String aad)throws Exception {return Framed.encrypt(new BufferedOutputStream(new FileOutputStream(f)),key,aad);}
    InputStream decrypted(File f,String aad)throws Exception {return Framed.decrypt(new BufferedInputStream(new FileInputStream(f)),key,aad);}
    List<Record> list()throws Exception {List<Record> out=new ArrayList<>();File[] fs=dir.listFiles();if(fs==null)throw new IOException("Vault is unreadable");for(File f:fs){if(!f.isDirectory()||!f.getName().matches("[a-f0-9-]{36}"))continue;try(InputStream in=decrypted(new File(f,"metadata"),f.getName()+":meta")){JSONObject m=new JSONObject(decode(bounded(in,1024*1024)));if(!f.getName().equals(m.getString("id")))throw new IOException("Vault identifier mismatch");out.add(new Record(m,f));}catch(Exception e){throw new IOException("Cannot authenticate vault record "+f.getName(),e);}}out.sort((a,b)->Long.compare(b.meta.optLong("imported_at"),a.meta.optLong("imported_at")));return out;}
    InputStream open(Record r)throws Exception{return decrypted(new File(r.dir,"original"),r.id()+":raw");}
    static void erase(File f){if(f.isDirectory()){File[] fs=f.listFiles();if(fs!=null)for(File child:fs)erase(child);}f.delete();}
    Record add(String provider,String account,String title,String mime,String source,InputStream in)throws Exception {
        JSONObject m=new JSONObject();m.put("id",UUID.randomUUID().toString());m.put("provider",provider);m.put("account",account);m.put("title",title);m.put("mime",mime);m.put("source",source);m.put("imported_at",System.currentTimeMillis());m.put("completeness","User-supplied source only; provider completeness not verified");
        return write(m,in,true);
    }
    void invalidate(){dedupIndex=null;}
    Record write(JSONObject m,InputStream in,boolean dedup)throws Exception {
        String id=m.getString("id");if(!id.matches("[a-f0-9-]{36}"))throw new IOException("Invalid record identifier");File tmp=new File(dir,"pending-"+UUID.randomUUID());if(!tmp.mkdir())throw new IOException("Cannot stage record");File dest=new File(dir,id);boolean committed=false;
        try {MessageDigest md=MessageDigest.getInstance("SHA-256");long size=0;try(OutputStream out=encrypted(new File(tmp,"original"),id+":raw")){byte[] b=new byte[65536];for(int n;(n=in.read(b))!=-1;){out.write(b,0,n);md.update(b,0,n);size+=n;}}
            String hash=hex(md.digest());if(m.has("sha256")&&!hash.equals(m.getString("sha256")))throw new IOException("Original file hash mismatch");if(m.has("size")&&size!=m.getLong("size"))throw new IOException("Original file size mismatch");m.put("sha256",hash);m.put("size",size);
            if(dedupIndex==null)dedupIndex=list();for(Record r:dedupIndex){if(r.id().equals(id)){if(!r.meta.optString("sha256").equals(hash))throw new IOException("Conflicting record ID");return r;}if(dedup&&r.provider().equals(m.optString("provider"))&&r.account().equals(m.optString("account"))&&r.title().equals(m.optString("title"))&&r.meta.optString("source_path").equals(m.optString("source_path"))&&r.meta.optString("sha256").equals(hash))return r;}
            try(OutputStream out=encrypted(new File(tmp,"metadata"),id+":meta")){out.write(utf8(m.toString()));}if(!tmp.renameTo(dest))throw new IOException("Cannot commit record");committed=true;Record result=new Record(m,dest);dedupIndex.add(result);return result;
        }finally{if(!committed)erase(tmp);}
    }
    String rawText(Record r)throws Exception {if(!isText(r))throw new IOException("Binary original; select ZIP to export intact.");try(InputStream in=open(r)){return decode(bounded(in,TEXT_LIMIT));}}
    static boolean isText(Record r){String m=r.kind().toLowerCase(Locale.ROOT), t=r.title().toLowerCase(Locale.ROOT);return m.startsWith("text/")||m.contains("json")||t.endsWith(".txt")||t.endsWith(".md")||t.endsWith(".json")||t.endsWith(".html")||t.endsWith(".csv");}
    String render(Record r)throws Exception {
        if(!isText(r))return "Binary original retained. Use ZIP to include the exact source bytes.\n";
        String s=rawText(r);if(!r.kind().contains("json")&&!r.title().toLowerCase(Locale.ROOT).endsWith(".json"))return s;
        // Render known ChatGPT graph records without dropping the source graph.
        try {JSONObject j=new JSONObject(s);JSONObject mapping=j.optJSONObject("mapping");if(mapping==null)return s;StringBuilder out=new StringBuilder();out.append("Conversation: ").append(j.optString("title",r.title())).append('\n');out.append("Current node: ").append(j.optString("current_node","unknown")).append('\n');out.append("All supplied graph nodes (not only current branch):\n");Iterator<String> keys=mapping.keys();while(keys.hasNext()){String id=keys.next();JSONObject node=mapping.optJSONObject(id);if(node==null){out.append(id).append(": ").append(mapping.opt(id)).append('\n');continue;}JSONObject msg=node.optJSONObject("message");out.append("\nNODE ").append(id).append(" | parent: ").append(node.opt("parent")).append(" | children: ").append(node.opt("children")).append('\n');if(msg==null){out.append("No message\n");continue;}JSONObject author=msg.optJSONObject("author");out.append(author==null?"unknown":author.optString("role","unknown")).append(" | time: ").append(msg.opt("create_time")).append('\n');JSONObject content=msg.optJSONObject("content");JSONArray parts=content==null?null:content.optJSONArray("parts");if(parts==null)out.append(msg.opt("content")).append('\n');else for(int i=0;i<parts.length();i++)out.append(parts.opt(i)).append('\n');}out.append("\n--- COMPLETE SUPPLIED SOURCE JSON ---\n").append(s);return out.toString();}catch(JSONException e){return s;}
    }
    boolean isZip(Record source){return source.title().toLowerCase(Locale.ROOT).endsWith(".zip")||source.kind().contains("zip");}
    private static Reader reader(InputStream in){return new InputStreamReader(in,StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT));}
    int expandChatGpt(Record source)throws Exception {
        if(!source.provider().equals("ChatGPT"))return 0;
        int count=0;String name=source.title().toLowerCase(Locale.ROOT);if(isZip(source)){
            try(java.util.zip.ZipInputStream zip=new java.util.zip.ZipInputStream(open(source))){for(java.util.zip.ZipEntry e;(e=zip.getNextEntry())!=null;){String n=e.getName();if(!e.isDirectory()&&(n.equals("conversations.json")||n.matches("(^|.*/)conversations(-[0-9]+)?\\.json"))){count+=expandArray(source,reader(zip));}zip.closeEntry();}}
        }else if(isText(source)&&(name.endsWith(".json")||source.kind().contains("json"))){try(InputStream in=open(source)){count+=expandArray(source,reader(in));}}
        return count;
    }
    private int expandArray(Record source,Reader reader)throws Exception {final int[] count={0};GraphArrayReader.read(reader,TEXT_LIMIT,json->{JSONObject j=new JSONObject(json);if(j.optJSONObject("mapping")==null)return;String name=j.optString("title","Conversation "+(count[0]+1));JSONObject m=new JSONObject();m.put("id",UUID.randomUUID().toString());m.put("provider",source.provider());m.put("account",source.account());m.put("title",name);m.put("mime","application/json");m.put("source","Imported graph from "+source.title());m.put("source_record",source.id());m.put("provider_id",j.optString("id",j.optString("conversation_id","")));for(String field:new String[]{"is_archived","project_id","create_time","update_time","current_node"})if(j.has(field))m.put(field,j.get(field));m.put("imported_at",System.currentTimeMillis());m.put("completeness","All nodes present in supplied graph; account completeness not verified");try(InputStream in=new ByteArrayInputStream(utf8(json))){write(m,in,true);}count[0]++;});return count[0];}
    int expandFiles(Record source)throws Exception {
        if(!isZip(source))throw new IOException("Select a ZIP source archive");int count=0;try(java.util.zip.ZipInputStream z=new java.util.zip.ZipInputStream(open(source))){for(java.util.zip.ZipEntry e;(e=z.getNextEntry())!=null;){if(!e.isDirectory()){String path=e.getName();String title=path.substring(path.lastIndexOf('/')+1);if(title.isEmpty())title="Archive entry";String ext=android.webkit.MimeTypeMap.getFileExtensionFromUrl(title);String mime=android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase(Locale.ROOT));if(mime==null)mime="application/octet-stream";JSONObject m=new JSONObject();m.put("id",UUID.randomUUID().toString());m.put("provider",source.provider());m.put("account",source.account());m.put("title",title);m.put("mime",mime);m.put("source","File from "+source.title());m.put("source_record",source.id());m.put("source_path",path);m.put("imported_at",System.currentTimeMillis());m.put("completeness","Exact supplied ZIP member; provider completeness not verified");write(m,z,true);count++;}z.closeEntry();}}return count;
    }
}
