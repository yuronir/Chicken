package com.nesswit.galbijjimsearcher;

import java.util.ArrayList;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

/**
 * HTML Parser for getting image from search results. 
 * @author nesswit
 *
 */
public class HTMLParser {
	
	public static ArrayList<Image> parseHtml(String html) {
		ArrayList<Image> images = new ArrayList<Image>();
		Source source = new Source(html);
		
		Element table = source.getAllElements(HTMLElementName.TABLE).get(0);
		Element div = table.getFirstElement().getFirstElement().getFirstElement().getFirstElement();
		
		ArrayList<Element> ps = new ArrayList<Element>(div.getAllElements(HTMLElementName.P));
		
		boolean isReallyP = true;
		for (Element p : ps) {
			if (p.getContent().toString().contains("이미지 결과 더 보기")) {
				isReallyP = false;
			}
			
			if (isReallyP && p.getAllElements(HTMLElementName.A).size() > 0) {
				Element a = p.getAllElements(HTMLElementName.A).get(0);
				Element img = a.getAllElements(HTMLElementName.IMG).get(0);
				String from = a.getAttributeValue("href");
				String url = img.getAttributeValue("src");
				Image image = new Image(url, from);
				image.debug = p.toString();
				images.add(image);
			}
		}
		
		return images;
	}
}
