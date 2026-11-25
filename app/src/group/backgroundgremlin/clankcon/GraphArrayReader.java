package group.backgroundgremlin.clankcon;
import java.io.*;

/** Streams exact object text from a top-level JSON array without loading the whole export. */
final class GraphArrayReader {
    interface Sink {void object(String json)throws Exception;}
    static int read(Reader source,int maxChars,Sink sink)throws Exception {
        BufferedReader in=source instanceof BufferedReader?(BufferedReader)source:new BufferedReader(source,65536);
        int c=space(in);if(c==0xfeff)c=space(in);if(c!='['){if(c=='{')return 0;throw new IOException("Expected conversation object or JSON array");}
        int count=0;c=space(in);if(c==']'){if(space(in)!=-1)throw new IOException("Trailing JSON content");return 0;}
        while(true){if(c!='{')throw new IOException("Expected a JSON object at array item "+(count+1));StringBuilder b=new StringBuilder();b.append((char)c);int depth=1;boolean string=false,escape=false;
            while(depth>0){c=in.read();if(c==-1)throw new EOFException("Incomplete conversation graph at item "+(count+1));if(b.length()>=maxChars)throw new IOException("Conversation graph "+(count+1)+" exceeds the per-graph parsing limit; complete original retained");b.append((char)c);
                if(string){if(escape)escape=false;else if(c=='\\')escape=true;else if(c=='\"')string=false;}else if(c=='\"')string=true;else if(c=='{'||c=='[')depth++;else if(c=='}'||c==']')depth--;}
            sink.object(b.toString());count++;c=space(in);if(c==']'){if(space(in)!=-1)throw new IOException("Trailing JSON content");return count;}if(c!=',')throw new IOException("Missing array separator after item "+count);c=space(in);
        }
    }
    static int space(Reader r)throws IOException{int c;do{c=r.read();}while(c==' '||c=='\n'||c=='\r'||c=='\t');return c;}
}
