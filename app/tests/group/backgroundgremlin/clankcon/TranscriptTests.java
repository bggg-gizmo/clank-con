package group.backgroundgremlin.clankcon;
import java.util.*;

public final class TranscriptTests {
    static Map<String,Object> map(Object... v){Map<String,Object> m=new LinkedHashMap<>();for(int i=0;i<v.length;i+=2)m.put((String)v[i],v[i+1]);return m;}
    public static void main(String[] args){
        Map<String,Object> u=map("role","user","content","東京 🤖\n```java\nint x=1;\n```","time","2026-01-01T00:00:00Z");
        Map<String,Object> a=map("role","assistant","content","Answer [1]","model","model-x","thinking","supplied thought","sources",Arrays.asList(map("title","Source","url","https://example.com")));
        Transcript t=Transcript.recognize(Arrays.asList(u,a));String r=t.render();HostTests.check(t.messages.size()==2&&r.indexOf("東京")<r.indexOf("Answer"),"AI Exporter-compatible message order and Unicode");HostTests.check(r.contains("```java")&&r.contains("model-x")&&r.contains("supplied thought")&&r.contains("https://example.com")&&r.contains("2026-01-01T00:00:00Z"),"message metadata, citations and code preserved");
        Map<String,Object> msg=map("index",0,"role","artifact","displayRole","Claude Artifact","content","artifact body","timestamp","2026-01-01");
        Map<String,Object> ace=map("$schema","export-v1.schema.json","schemaVersion",1,"source",map("platform","Claude","title","Example","url",null),"exportedAt","2026-01-01T00:00:00Z","metadata",map("custom",true),"messages",Arrays.asList(msg));
        t=Transcript.recognize(ace);HostTests.check(t!=null&&t.platform.equals("Claude")&&t.render().contains("Claude Artifact")&&t.render().contains("artifact body"),"ACE v1 source and artifact role");
        ace.put("schemaVersion",2);HostTests.check(Transcript.recognize(ace)==null,"unknown ACE version rejected");ace.put("schemaVersion",1);msg.put("index",-1);HostTests.check(Transcript.recognize(ace)==null,"invalid message index rejected");
        HostTests.check(Transcript.recognize(Arrays.asList(u,map("role","assistant","content",42)))==null,"malformed message not silently dropped");
        HostTests.check(Transcript.recognize(Collections.emptyList())==null&&Transcript.recognize(map("mapping",map("a",map())))==null,"ambiguous empty array and graph not misclassified");
    }
}
