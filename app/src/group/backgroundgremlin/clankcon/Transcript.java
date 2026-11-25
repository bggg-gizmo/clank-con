package group.backgroundgremlin.clankcon;

import java.util.*;

/** Offline readers for supplied ACE v1 and role/content JSON exports. No network access. */
final class Transcript {
    final String format, platform, title, url, exportedAt;
    final List<Map<String,Object>> messages;
    private Transcript(String f,String p,String t,String u,String e,List<Map<String,Object>> m){format=f;platform=p;title=t;url=u;exportedAt=e;messages=Collections.unmodifiableList(m);}
    static String str(Map<?,?> m,String key){Object x=m.get(key);return x instanceof String?(String)x:"";}
    static boolean role(String r){return Arrays.asList("user","assistant","system","tool","artifact","unknown").contains(r);}
    static Transcript recognize(Object root){
        boolean ace=root instanceof Map;
        List<?> rows;String platform="",title="",url="",exported="";
        if(ace){Map<?,?> m=(Map<?,?>)root;Object version=m.get("schemaVersion"), source=m.get("source"), data=m.get("messages");
            if(!(version instanceof Number)||((Number)version).doubleValue()!=1||!(m.get("$schema") instanceof String)||!(source instanceof Map)||!(data instanceof List)||!(m.get("metadata") instanceof Map)||!(m.get("exportedAt") instanceof String))return null;
            Map<?,?> s=(Map<?,?>)source;if(!(s.get("platform") instanceof String)||!(s.get("title") instanceof String)||!s.containsKey("url")||(s.get("url")!=null&&!(s.get("url") instanceof String)))return null;
            platform=str(s,"platform");title=str(s,"title");url=str(s,"url");exported=str(m,"exportedAt");rows=(List<?>)data;
        }else if(root instanceof List){rows=(List<?>)root;}else return null;
        if(rows.isEmpty()&&!ace)return null;
        List<Map<String,Object>> out=new ArrayList<>();
        for(Object item:rows){if(!(item instanceof Map))return null;Map<?,?> m=(Map<?,?>)item;String role=str(m,"role");if(!(m.get("content") instanceof String))return null;
            if(ace){Object i=m.get("index");if(!role(role)||!(m.get("displayRole") instanceof String)||!(i instanceof Number)||((Number)i).doubleValue()<0||((Number)i).doubleValue()!=Math.floor(((Number)i).doubleValue()))return null;}
            else if(!role.equals("user")&&!role.equals("assistant"))return null;
            Map<String,Object> copy=new LinkedHashMap<>();for(Map.Entry<?,?> e:m.entrySet()){if(!(e.getKey() instanceof String))return null;copy.put((String)e.getKey(),e.getValue());}out.add(Collections.unmodifiableMap(copy));
        }
        return new Transcript(ace?"ACE schema v1":"Role/content JSON (AI Exporter-compatible)",platform,title,url,exported,out);
    }
    String render(){StringBuilder b=new StringBuilder();b.append("Supplied format: ").append(format).append("\nCoverage: supplied messages only; provider history and branches not verified.\n");if(!platform.isEmpty())b.append("Source platform: ").append(platform).append('\n');if(!title.isEmpty())b.append("Source title: ").append(title).append('\n');if(!url.isEmpty())b.append("Source URL: ").append(url).append('\n');if(!exportedAt.isEmpty())b.append("Exported at: ").append(exportedAt).append('\n');b.append("Messages: ").append(messages.size()).append('\n');
        for(int i=0;i<messages.size();i++){Map<String,Object> m=messages.get(i);b.append("\n--- Message ").append(i+1).append(" | ").append(str(m,"role"));String display=str(m,"displayRole");if(!display.isEmpty())b.append(" / ").append(display);b.append(" ---\n");for(String key:Arrays.asList("index","id","timestamp","time","model")){if(m.containsKey(key))b.append(key).append(": ").append(m.get(key)).append('\n');}b.append(str(m,"content")).append('\n');if(m.containsKey("thinking"))b.append("\nSupplied thinking field:\n").append(m.get("thinking")).append('\n');Object sources=m.get("sources");if(sources instanceof List){b.append("\nSupplied sources:\n");for(Object s:(List<?>)sources){if(s instanceof Map){b.append(str((Map<?,?>)s,"title")).append(" | ").append(str((Map<?,?>)s,"url")).append('\n');}else b.append(s).append('\n');}}}
        return b.toString();
    }
}
