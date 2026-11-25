package group.backgroundgremlin.clankcon;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;

/** Bounded-memory, sequenced AES-GCM frames with authenticated end marker. */
final class Framed {
    static final int BLOCK=65536;
    static byte[] aad(String context,long seq){byte[] s=context.getBytes(StandardCharsets.UTF_8);return ByteBuffer.allocate(s.length+8).put(s).putLong(seq).array();}
    static OutputStream encrypt(OutputStream base,SecretKey key,String context){return new Output(base,key,context);}
    static InputStream decrypt(InputStream base,SecretKey key,String context){return new Input(base,key,context);}
    private static final class Output extends OutputStream {
        final DataOutputStream out; final SecretKey key; final String context; final byte[] b=new byte[BLOCK]; int len; long seq; boolean closed;
        Output(OutputStream o,SecretKey k,String c){out=new DataOutputStream(o);key=k;context=c;}
        void frame(int n)throws IOException {try{Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,key);cipher.updateAAD(aad(context,seq++));byte[] iv=cipher.getIV();if(iv.length!=12)throw new IOException("Invalid GCM nonce");byte[] enc=cipher.doFinal(b,0,n);out.writeInt(enc.length);out.write(iv);out.write(enc);}catch(GeneralSecurityException e){throw new IOException("Encryption failed",e);}}
        @Override public void write(int x)throws IOException {ensure();b[len++]=(byte)x;if(len==BLOCK){frame(len);len=0;}}
        @Override public void write(byte[] x,int off,int n)throws IOException{ensure();if(off<0||n<0||off>x.length-n)throw new IndexOutOfBoundsException();while(n>0){int k=Math.min(n,BLOCK-len);System.arraycopy(x,off,b,len,k);len+=k;off+=k;n-=k;if(len==BLOCK){frame(len);len=0;}}}
        void ensure()throws IOException{if(closed)throw new IOException("Stream closed");}
        @Override public void close()throws IOException{if(closed)return;closed=true;try{if(len>0)frame(len);frame(0);out.flush();}finally{java.util.Arrays.fill(b,(byte)0);out.close();}}
    }
    private static final class Input extends InputStream {
        final DataInputStream in;final SecretKey key;final String context;byte[] b=new byte[0];int pos;long seq;boolean ended,closed,failed;
        Input(InputStream i,SecretKey k,String c){in=new DataInputStream(i);key=k;context=c;}
        boolean next()throws IOException{if(ended)return false;try{int n=in.readInt();if(n<16||n>BLOCK+16)throw new IOException("Invalid encrypted frame size");byte[] iv=new byte[12],enc=new byte[n];in.readFully(iv);in.readFully(enc);Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,key,new GCMParameterSpec(128,iv));c.updateAAD(aad(context,seq++));b=c.doFinal(enc);pos=0;if(b.length==0){ended=true;if(in.read()!=-1)throw new IOException("Unexpected trailing encrypted data");return false;}return true;}catch(GeneralSecurityException|IOException e){failed=true;throw new IOException("Encrypted data failed authentication or is incomplete",e);}}
        @Override public int read()throws IOException {if(closed)throw new IOException("Stream closed");if(pos==b.length&&!next())return -1;return b[pos++]&255;}
        @Override public int read(byte[] dst,int off,int n)throws IOException{if(closed)throw new IOException("Stream closed");if(off<0||n<0||off>dst.length-n)throw new IndexOutOfBoundsException();if(n==0)return 0;if(pos==b.length&&!next())return -1;int k=Math.min(n,b.length-pos);System.arraycopy(b,pos,dst,off,k);pos+=k;return k;}
        @Override public void close()throws IOException{if(closed)return;try{if(!failed&&!ended){byte[] tmp=new byte[BLOCK];while(read(tmp)!=-1){}java.util.Arrays.fill(tmp,(byte)0);}}finally{closed=true;java.util.Arrays.fill(b,(byte)0);in.close();}}
    }
}
