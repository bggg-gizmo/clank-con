package group.backgroundgremlin.clankcon;
import java.io.*;
import java.security.SecureRandom;
import java.util.*;
import java.util.zip.*;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.json.*;

final class Backup {
    static final byte[] MAGIC=Vault.utf8("CLANKBAK1");
    static SecretKey derive(char[] pass,byte[] salt)throws Exception{PBEKeySpec spec=new PBEKeySpec(pass,salt,600000,256);try{byte[] b=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();try{return new SecretKeySpec(b,"AES");}finally{Arrays.fill(b,(byte)0);}}finally{spec.clearPassword();}}
    static void save(Vault v,char[] pass,OutputStream out)throws Exception{
        byte[] salt=new byte[16];new SecureRandom().nextBytes(salt);out.write(MAGIC);out.write(salt);SecretKey key=derive(pass,salt);
        try(ZipOutputStream z=new ZipOutputStream(Framed.encrypt(out,key,"clank-backup-v1:"+Vault.hex(salt)))){List<Vault.Record> rs=v.list();JSONArray a=new JSONArray();for(Vault.Record r:rs)a.put(r.meta);JSONObject manifest=new JSONObject();manifest.put("schema","clank-con-backup-1");manifest.put("records",a);Exports.entry(z,"manifest.json",manifest.toString());for(Vault.Record r:rs){z.putNextEntry(new ZipEntry(r.id()+"/original"));try(InputStream in=v.open(r)){Vault.copy(in,z);}z.closeEntry();}}
    }
    static int restore(Vault v,char[] pass,InputStream base)throws Exception{
        DataInputStream head=new DataInputStream(base);byte[] magic=new byte[MAGIC.length],salt=new byte[16];head.readFully(magic);head.readFully(salt);if(!Arrays.equals(magic,MAGIC))throw new IOException("Not a Clank-Con encrypted backup");SecretKey key=derive(pass,salt);File stageDir=new File(v.dir.getParentFile(),"restore-"+UUID.randomUUID());Vault stage=new Vault(stageDir,v.key);List<File> moved=new ArrayList<>();
        try{try(ZipInputStream z=new ZipInputStream(Framed.decrypt(head,key,"clank-backup-v1:"+Vault.hex(salt)))){ZipEntry e=z.getNextEntry();if(e==null||!e.getName().equals("manifest.json"))throw new IOException("Missing backup manifest");JSONObject manifest=new JSONObject(Vault.decode(Vault.bounded(z,16*1024*1024)));if(!"clank-con-backup-1".equals(manifest.getString("schema")))throw new IOException("Unsupported backup version");JSONArray a=manifest.getJSONArray("records");z.closeEntry();Set<String> ids=new HashSet<>();for(int i=0;i<a.length();i++){JSONObject m=a.getJSONObject(i);String id=m.getString("id");if(!ids.add(id))throw new IOException("Duplicate backup ID");e=z.getNextEntry();if(e==null||!e.getName().equals(id+"/original"))throw new IOException("Missing backup original");stage.write(m,z,false);z.closeEntry();}if(z.getNextEntry()!=null)throw new IOException("Unexpected backup entries");} // close authenticates terminal frame before committing anything
            Map<String,Vault.Record> current=new HashMap<>();for(Vault.Record r:v.list())current.put(r.id(),r);List<Vault.Record> pending=stage.list();for(Vault.Record r:pending){Vault.Record old=current.get(r.id());if(old!=null&&!old.meta.toString().equals(r.meta.toString()))throw new IOException("Conflicting backup record: "+r.id());}
            for(Vault.Record r:pending){if(current.containsKey(r.id()))continue;File dest=new File(v.dir,r.id());if(!r.dir.renameTo(dest))throw new IOException("Cannot commit restored record");moved.add(dest);}v.invalidate();return moved.size();
        }catch(Exception e){for(File f:moved)Vault.erase(f);v.invalidate();throw e;}finally{Vault.erase(stageDir);}
    }
}
