package com.kevin.utils;

import com.kevin.model.DocContent;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

public class XmlParser {

    public static DocContent parse(String filePath){

        DocContent docContent = new DocContent();
        SAXReader reader = new SAXReader();


        try {
            reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",false);
            Document document = reader.read(new FileInputStream(new File(filePath)));
            Element root = document.getRootElement();

            List docNodes = document.selectNodes("//cn-patent-document/cn-bibliographic-data/cn-publication-reference/document-id");

            if (docNodes.size() > 0){
                Element docNone = (Element)docNodes.get(0);
                StringBuilder docId = new StringBuilder();
                docId.append(docNone.element("country").getText()).append(docNone.element("doc-number").getText()).append(docNone.element("kind").getText());
                docContent.setDocId(docId.toString());
//                System.out.println(docId.toString());

            }

            List appNodes = document.selectNodes("//cn-patent-document/cn-bibliographic-data/application-reference/document-id");

            if (appNodes.size() >0){
                Element appNone = (Element)appNodes.get(0);
                StringBuilder appId = new StringBuilder();
                appId.append(appNone.element("country").getText()).append(appNone.element("doc-number").getText());
                docContent.setAppId(appId.toString());

//                System.out.println(appId.toString());
            }

            List<Element> ipcNodes = document.selectNodes("//cn-patent-document/cn-bibliographic-data/classifications-ipcr/classification-ipcr");

            if (ipcNodes.size() >0){

                StringBuilder ipcs = new StringBuilder();
                for (int i=0;i<ipcNodes.size();i++){
                    String ipc = ipcNodes.get(i).elementText("text");
//                    String[] tempIpcs = ipc.split(" ");


//                    ipcs.append(tempIpcs[0]+" "+tempIpcs[1]);
                    ipcs.append(ipc);
                    if (i != ipcNodes.size()-1){
                        ipcs.append(",");
                    }
                }
                docContent.setCategory(ipcs.toString());

//                System.out.println(ipcs.toString());
            }

            List titleNodes = document.selectNodes("//cn-patent-document/cn-bibliographic-data/invention-title");

            if (titleNodes.size() >0){
                Element titleNode = (Element)titleNodes.get(0);

                docContent.setTitle(titleNode.getText());

//                System.out.println(titleNode.getText());
            }

            List absNodes = document.selectNodes("//cn-patent-document/cn-bibliographic-data/abstract");

            if (absNodes.size() >0){
                Element absNode = (Element)absNodes.get(0);

                List<Element> elements = absNode.elements("p");

                StringBuilder abs = new StringBuilder();
                for (int i=0;i<elements.size();i++ ) {
                    Element element = elements.get(i);
                    String content = StringUtil.remove(element.getText());
                    abs.append(content);
                    if (i != elements.size()-1){
                        abs.append("\n");
                    }
                }

                docContent.setAbs(abs.toString());

//                System.out.println(abs.toString());
            }

            List<Element> claimsNodes = document.selectNodes("//cn-patent-document/application-body/claims/claim");

            if (claimsNodes.size() >0){

                StringBuilder claims = new StringBuilder();
                for (int i=0;i<claimsNodes.size();i++ ) {

                    Element claimNode = claimsNodes.get(i);

                    List<Element> elements = claimNode.elements("claim-text");
                    StringBuilder claim = new StringBuilder();
                    for (int j=0;j<elements.size();j++ ) {
                        Element element = elements.get(j);
                        String content = StringUtil.remove(element.getText());
                        claim.append(content);

                    }
                    claims.append(claim.toString());

                    if (i != claimsNodes.size()-1){
                        claims.append("\n");
                    }
                }
                docContent.setClaims(claims.toString());

//                System.out.println(claims.toString());
            }

            List descNodes = document.selectNodes("//cn-patent-document/application-body/description");

            if (descNodes.size() >0){
                Element descNode = (Element)descNodes.get(0);

                List<Element> elements = descNode.elements("p");

                StringBuilder desc = new StringBuilder();
                for (int i=0;i<elements.size();i++ ) {
                    Element element = elements.get(i);
                    String content = StringUtil.remove(element.getText());
                    desc.append(content);
                    if (i != elements.size()-1){
                        desc.append("\n");
                    }
                }

                docContent.setDesc(desc.toString());

//                System.out.println(desc.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return docContent;
    }

    public static void main(String[] args){
        String path = "J:/kevin/patentdata/2017/1052/2017105252498.xml";

        XmlParser xmlParser = new XmlParser();
        xmlParser.parse(path);

    }


}
