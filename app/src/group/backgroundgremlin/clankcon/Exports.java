package group.backgroundgremlin.clankcon;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Base64;
import android.util.Base64OutputStream;
import org.json.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

final class Exports {
    static final String[] FORMATS={"TXT","PDF","JSON","Markdown","HTML","DOCX","ZIP"};
    static final String[] EXT={"txt","pdf","json","md","html","docx","zip"};
    static final String[] MIME={"text/plain","application/pdf","application/json","text/markdown","text/html","application/vnd.openxmlformats-officedocument.wordprocessingml.document","application/zip"};
    static void write(OutputStream o,String s)throws IOException{o.write(Vault.utf8(s));}
    static String header(Vault.Record r){return r.title()+"\nProvider: "+r.provider()+" | Account: "+r.account()+"\nSource: "+r.meta.optString("source")+"\nCompleteness: "+r.meta.optString("completeness")+"\nSHA-256: "+r.meta.optString("sha256")+"\n\n";}
    static String xml(String s){StringBuilder b=new StringBuilder();for(int i=0;i<s.length();){int c=s.codePointAt(i);i+=Character.charCount(c);switch(c){case '&':b.append("&amp;");break;case '<':b.append("&lt;");break;case '>':b.append("&gt;");break;case '"':b.append("&quot;");break;case '\'':b.append("&apos;");break;default:if(c==9||c==10||c==13||c>=32&&c<=0xd7ff||c>=0xe000&&c<=0xfffd||c>=0x10000&&c<=0x10ffff)b.appendCodePoint(c);else b.append(String.format(Locale.ROOT,"[U+%04X]",c));}}return b.toString();}
    static void entry(ZipOutputStream z,String n,String s)throws IOException{z.putNextEntry(new ZipEntry(n));write(z,s);z.closeEntry();}
    static String safe(String n){String s=n.replaceAll("[\\p{Cntrl}/\\\\:*?\"<>|]","_");if(s.length()>120)s=s.substring(0,120);return s.isEmpty()||s.equals(".")||s.equals("..")?"original":s;}
    static void export(Vault v,List<Vault.Record> records,int f,OutputStream out)throws Exception {
        if(records.isEmpty())throw new IOException("No selected records");
        if(f==6){zip(v,records,out);return;}if(f==2){json(v,records,out);return;}if(f==1){pdf(v,records,out);return;}if(f==5){docx(v,records,out);return;}
        if(f==4)write(out,"<!doctype html><html lang=\"en\"><meta charset=\"utf-8\"><title>Clank-Con Archive</title><style>body{font-family:system-ui;max-width:80ch;margin:2em auto;padding:1em}pre{white-space:pre-wrap;overflow-wrap:anywhere}</style><body><h1>Clank-Con Archive</h1>");
        for(Vault.Record r:records){String text=header(r)+v.render(r);if(f==4)write(out,"<section><pre>"+xml(text)+"</pre></section>\n");else if(f==3){write(out,"## "+r.title().replace("\n"," ").replace("\r"," ")+"\n\n");String fence="````";while(text.contains(fence))fence+="`";write(out,fence+"text\n"+text+"\n"+fence+"\n\n");}else write(out,text+"\n\n============================================================\n\n");}
        if(f==4)write(out,"</body></html>");out.flush();
    }
    static void json(Vault v,List<Vault.Record> rs,OutputStream o)throws Exception{write(o,"{\"schema\":\"clank-con-export-1\",\"records\":[");boolean first=true;for(Vault.Record r:rs){if(!first)write(o,",");first=false;write(o,"{\"metadata\":"+r.meta.toString()+",\"original_base64\":\"");try(InputStream in=v.open(r);Base64OutputStream b=new Base64OutputStream(o,Base64.NO_WRAP|Base64.NO_CLOSE)){Vault.copy(in,b);}write(o,"\"}");}write(o,"]}");o.flush();}
    static void zip(Vault v,List<Vault.Record> rs,OutputStream o)throws Exception{try(ZipOutputStream z=new ZipOutputStream(o)){JSONArray a=new JSONArray();for(Vault.Record r:rs){JSONObject m=new JSONObject(r.meta.toString());String n="originals/"+r.id()+"/"+safe(r.title());m.put("archive_path",n);a.put(m);z.putNextEntry(new ZipEntry(n));try(InputStream in=v.open(r)){Vault.copy(in,z);}z.closeEntry();}JSONObject m=new JSONObject();m.put("schema","clank-con-zip-1");m.put("records",a);entry(z,"manifest.json",m.toString(2));}}
    static void pdf(Vault v,List<Vault.Record> rs,OutputStream o)throws Exception {
        final int width=595,height=842,margin=42;TextPaint p=new TextPaint(Paint.ANTI_ALIAS_FLAG);p.setColor(Color.BLACK);p.setTextSize(11);Paint foot=new Paint(Paint.ANTI_ALIAS_FLAG);foot.setTextSize(9);int pageNo=0;
        PdfDocument pdf=new PdfDocument();try{for(Vault.Record r:rs){String text=header(r)+v.render(r);StaticLayout layout=StaticLayout.Builder.obtain(text,0,text.length(),p,width-2*margin).setAlignment(Layout.Alignment.ALIGN_NORMAL).setIncludePad(false).build();int line=0;while(line<layout.getLineCount()){int end=line+1;int top=layout.getLineTop(line);while(end<layout.getLineCount()&&layout.getLineBottom(end)-top<=height-2*margin-18)end++;PdfDocument.Page page=pdf.startPage(new PdfDocument.PageInfo.Builder(width,height,++pageNo).create());Canvas c=page.getCanvas();c.save();c.clipRect(margin,margin,width-margin,margin+layout.getLineBottom(end-1)-top);c.translate(margin,margin-top);layout.draw(c);c.restore();c.drawText("Clank-Con | "+pageNo,margin,height-22,foot);pdf.finishPage(page);line=end;}}pdf.writeTo(o);}finally{pdf.close();}
    }
    static void docx(Vault v,List<Vault.Record> rs,OutputStream o)throws Exception {
        try(ZipOutputStream z=new ZipOutputStream(o)){entry(z,"[Content_Types].xml","<?xml version=\"1.0\" encoding=\"UTF-8\"?><Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\"><Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/><Default Extension=\"xml\" ContentType=\"application/xml\"/><Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/></Types>");entry(z,"_rels/.rels","<?xml version=\"1.0\" encoding=\"UTF-8\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/></Relationships>");z.putNextEntry(new ZipEntry("word/document.xml"));write(z,"<?xml version=\"1.0\" encoding=\"UTF-8\"?><w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body>");for(Vault.Record r:rs){String t=header(r)+v.render(r);for(String line:t.split("\\r\\n|\\n|\\r",-1)){write(z,"<w:p><w:r><w:t xml:space=\"preserve\">");write(z,xml(line).replace("\t","</w:t><w:tab/><w:t xml:space=\"preserve\">"));write(z,"</w:t></w:r></w:p>");}write(z,"<w:p><w:r><w:br w:type=\"page\"/></w:r></w:p>");}write(z,"<w:sectPr><w:pgSz w:w=\"11906\" w:h=\"16838\"/><w:pgMar w:top=\"1134\" w:right=\"1134\" w:bottom=\"1134\" w:left=\"1134\"/></w:sectPr></w:body></w:document>");z.closeEntry();}}
}
