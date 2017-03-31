package com.javbus;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.javbus.entity.MovieInfo;

public class Main {

	// 配置文件视频文件夹
	static String ROOT = "F:\\____temp";

	String[] suffix = { ".jpg", ".png", ".avi", ".wmv", ".mp4", ".mpg", ".mpeg", ".flv", ".mkv", ".rmvb", ".mov",
			".iso", ".nfo" };

	public static void main(String[] args) throws Exception {
		File root = new File(ROOT);
		File[] listFiles = root.listFiles();
		for (File file : listFiles) {
			if (file.isFile()) {
				String filename = file.getName();
				String num = filename.substring(0, filename.lastIndexOf("."));
				// getInfo(num);
			}
		}
		getInfo("MIaE-015");
	}

	public static void getInfo(String num) throws Exception {
		MovieInfo movie = new MovieInfo();
		String baseurl = "https://www.javbus.com/";
		Document doc;
		try {
			doc = Jsoup.connect(baseurl + num).get();

			String title = doc.select("h3").text();
			System.out.println(title);
			// 封面
			Elements mainimg = doc.select(".bigImage");
			movie.setCover(mainimg.first().attr("href"));

			// 截图
			Elements Screenshot = doc.select(".sample-box");
			List<String> previews = new ArrayList<String>();
			for (Element element : Screenshot) {
				previews.add(element.attr("href"));
			}
			movie.setPreviews(previews);

			// 有没有码
			Elements classificationElements = doc.select("li[class=active]");
			String classification = classificationElements.text();
			movie.setCensored(classification);

			// 详细信息
			Elements info = doc.select(".info");
			Elements infos = info.select("p");
			for (int i = 0; i < infos.size(); i++) {
				String header = infos.get(i).select(".header").text();
				if ("識別碼:".equals(header)) {
					movie.setNum(infos.get(i).text().replaceAll(header + " ", ""));
					title = title.replaceAll(movie.getNum(), "");
				} else if ("發行日期:".equals(header)) {
					movie.setRelease(infos.get(i).text().replaceAll(header + " ", ""));
				} else if ("長度:".equals(header)) {
					movie.setRunTime(infos.get(i).text().replaceAll(header + " ", ""));
				} else if ("導演:".equals(header)) {
					movie.setDirector(infos.get(i).text().replaceAll(header + " ", ""));
				} else if ("製作商:".equals(header)) {
					movie.setStudio(infos.get(i).text().replaceAll(header + " ", ""));
				} else if ("發行商:".equals(header)) {
					movie.setLabel(infos.get(i).text().replaceAll(header + " ", ""));
				} else if ("系列:".equals(header)) {
					movie.setSeries(infos.get(i).text().replaceAll(header + " ", ""));
				} else if ("類別:".equals(header) || "演員".equals(header)) {
					// System.err.println("特殊字段");
				} else {
					// System.err.println(header+"未识别的字段");
				}
			}

			// 类别
			List<String> genres = new ArrayList<String>();
			Elements select = infos.select(".genre");
			for (Element element : select) {
				if (!element.html().contains("star")) {
					genres.add(element.text());
				}
			}
			movie.setGenres(genres);

			// 演员
			List<String> stars = new ArrayList<>();
			Elements star = infos.select("span[onmouseover]");
			for (Element element : star) {
				stars.add(element.text());
				title = title.replaceAll(element.text(), "");
			}
			movie.setStars(stars);
			movie.setTitle(title.trim());

			System.out.println(movie);
			// System.out.println(info);
			
			
			//获取磁力链接
			Elements e = doc.getElementsByTag("script").eq(8);
			Map<String, String> jsParams = getJsParams(e);
			String url = "https://www.javbus.com/ajax/uncledatoolsbyajax.php";
			Connection con = Jsoup.connect(url);
			con.data(jsParams);
			con.header("referer", baseurl + num);
			Document document = con.get();
			
			// 磁力链接
			Elements Magnets = document.select("a[title=滑鼠右鍵點擊並選擇【複製連結網址】]");
			System.out.println(document);
			for (int j = 0; j < Magnets.size(); j++) {
				if (j%3==0) {
					System.out.println(Magnets.get(j).text());
					
				}
			}

		} catch (HttpStatusException e) {
			System.out.println(num + "不存在");
		}
	}

	public static Map<String, String> getJsParams(Elements e) {
		/* 用來封裝要保存的参数 */
		Map<String, String> map = new HashMap<String, String>();
		for (Element element : e) {

			/* 取得JS变量数组 */
			String[] data = element.data().toString().split("var");

			/* 取得单个JS变量 */
			for (String variable : data) {

				/* 过滤variable为空的数据 */
				if (variable.contains("=")) {

					/* 取到满足条件的JS变量 */
					if (variable.contains("gid") || variable.contains("uc") || variable.contains("color")
							|| variable.contains("img")) {

						String[] kvp = variable.split("=");

						/* 取得JS变量存入map */
						if (!map.containsKey(kvp[0].trim()))
							map.put(kvp[0].trim(), kvp[1].trim().substring(0, kvp[1].trim().length() - 1).toString());
					}
				}
			}
		}
		map.put("lang", "zh");
		return map;
	}
}
