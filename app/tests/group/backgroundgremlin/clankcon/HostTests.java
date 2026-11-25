package group.backgroundgremlin.clankcon;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.SecretKey;

public final class HostTests {
 static final SecretKey KEY=new SecretKeySpec(new byte[32],"AES");
 static void check(boolean condition,String name){if(!condition)throw new AssertionError(name);System.out.println("PASS: "+name);}
 static byte[] seal(byte[] raw)throws Exception{ByteArrayOutputStream b=new ByteArrayOutputStream();try(OutputStream o=Framed.encrypt(b,KEY,"test-record")){o.write(raw);}return b.toByteArray();}
 static byte[] unseal(byte[] enc,SecretKey key,String ctx)throws Exception{ByteArrayOutputStream b=new ByteArrayOutputStream();try(InputStream in=Framed.decrypt(new ByteArrayInputStream(enc),key,ctx)){byte[] block=new byte[1733];for(int n;(n=in.read(block))!=-1;)b.write(block,0,n);}return b.toByteArray();}
 static boolean rejects(byte[] enc,SecretKey key,String ctx){try{unseal(enc,key,ctx);return false;}catch(IOException expected){return true;}catch(Exception e){throw new RuntimeException(e);}}
 public static void main(String[] args)throws Exception {
   byte[] raw=new byte[7*Framed.BLOCK+1021];new Random(42).nextBytes(raw);byte[] enc=seal(raw);
   check(Arrays.equals(raw,unseal(enc,KEY,"test-record")),"multi-frame byte-for-byte round trip");
   check(Arrays.equals(new byte[0],unseal(seal(new byte[0]),KEY,"test-record")),"empty authenticated stream");
   check(!Arrays.equals(seal(raw),enc),"randomized ciphertext");
   byte[] tampered=enc.clone();tampered[60]^=1;check(rejects(tampered,KEY,"test-record"),"ciphertext tamper rejection");
   byte[] other=new byte[32];other[0]=1;check(rejects(enc,new SecretKeySpec(other,"AES"),"test-record"),"wrong key rejection");
   check(rejects(enc,KEY,"wrong-record"),"record identity substitution rejection");
   check(rejects(Arrays.copyOf(enc,enc.length-32),KEY,"test-record"),"missing end marker rejection");
   check(rejects(Arrays.copyOf(enc,enc.length+1),KEY,"test-record"),"trailing bytes rejection");
   int frame=4+12+Framed.BLOCK+16;byte[] reordered=enc.clone();System.arraycopy(enc,frame,reordered,0,frame);System.arraycopy(enc,0,reordered,frame,frame);check(rejects(reordered,KEY,"test-record"),"frame reorder rejection");
   String a="{\"title\":\"emoji 🤖 and \\\"quoted\\\" braces } [\",\"mapping\":{\"a\":{\"children\":[\"b\"]}}}";String b="{\"title\":\"東京\",\"mapping\":{}}";ArrayList<String> objs=new ArrayList<>();int count=GraphArrayReader.read(new StringReader("\uFEFF [\n"+a+",\n"+b+"]\n"),4096,objs::add);
   check(count==2&&objs.get(0).equals(a)&&objs.get(1).equals(b),"streamed graph object source preservation with Unicode and escapes");
   check(GraphArrayReader.read(new StringReader("[ ]"),100,x->{throw new AssertionError();})==0,"empty graph export");
   boolean failed=false;try{GraphArrayReader.read(new StringReader("["+a),4096,x->{});}catch(IOException e){failed=true;}check(failed,"truncated array rejection");
   failed=false;try{GraphArrayReader.read(new StringReader("["+a+"]"),10,x->{});}catch(IOException e){failed=true;}check(failed,"explicit per-object limit rejection");
 }
}
