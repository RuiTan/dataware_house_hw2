package top.guitoubing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class Main {

    /**
     *
     */
    static class MovieData{
        private String title;
        private String director;
        private ArrayList<String> actors;

        public MovieData() {
            title = "";
            director = "";
            actors = new ArrayList<String>();
        }

        public MovieData(String title, String director, ArrayList<String> actors) {
            this.title = title;
            this.director = director;
            this.actors = actors;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDirector() {
            return director;
        }

        public void setDirector(String director) {
            this.director = director;
        }

        public ArrayList<String> getActors() {
            return actors;
        }

        public void setActors(ArrayList<String> actors) {
            this.actors = actors;
        }

        public boolean addActors(String actor){
            if (actor.equals("")){
                return false;
            }else {
                actors.add(actor);
                return true;
            }
        }

        @Override
        public String toString() {
            StringBuilder data = new StringBuilder();
            data.append("Title : ").append(title).append("\n")
                    .append("Director : ").append(director).append("\n")
                    .append("Actors : ");
            for (String actor : actors){
                data.append(actor).append("、");
            }
            data.replace(data.length()-1, data.length(), "\n");
            return data.toString();
        }
    }

    public static void main(String[] args) throws SQLException, IOException {
        // TODO Auto-generated method stub
        Connection con = DriverManager.getConnection("jdbc:neo4j://localhost:7474","neo4j","tanrui");

        File folder = new File("/Users/tanrui/Desktop/1");

        File out = new File("/Users/tanrui/Desktop/out.txt");
        FileWriter fileWriter = new FileWriter(out);

        if (folder.exists()){
            for (File file : folder.listFiles()){
                // 解析文件
                Document doc = Jsoup.parse(ParseFile(file));
                MovieData data = ParseDoc(doc);

                if (data == null){
                    System.out.println("Null");
                }else {
                    System.out.println(data);
                    fileWriter.write(data.toString());
                }
            }
        }

        fileWriter.close();


//        // 获取文件
//        File file = new File("/Users/tanrui/Desktop/1/0001517791.html");
//        // 解析文件
//        Document doc = Jsoup.parse(ParseFile(file));
//        MovieData data = ParseDoc(doc);
//
//        if (data == null){
//            System.out.println("Null");
//        }else {
//            System.out.println(data);
//        }

        // 执行query
//        Statement stmt = con.createStatement();
//        ResultSet rs = stmt.executeQuery("MATCH (n:D1) RETURN n");
//        while (rs.next()) {
//            System.out.println(rs.getString(1));
//        }
    }

    /**
     *
     * @param document Jsoup解析文件
     * @return 生成一个MovieData实例
     */
    public static MovieData ParseDoc(Document document) throws NullPointerException{
        MovieData data = new MovieData();
        // 判断页面格式
        Elements e = document.select("[id=\"productTitle\"]");
        // Movie & DVD 页面
        if (e != null){
            // 设置标题
            data.setTitle(e.text().trim());
            // 获取导演、演员信息
            Elements elements = document.select("[id=\"detail-bullets\"]>table>tbody>tr>td>div>ul>li");
            // 判断是否为电影页面
            boolean flagActor = false, flagDirector = false;
            if (elements.get(0).text().contains("Actors"))    flagActor = true;
            if (elements.get(1).text().contains("Directors")) flagDirector = true;
            if (!(flagActor && flagDirector)) return null;
            // 设置导演
            data.setDirector(elements.get(1).select("a").text());
            // 添加演员
            Elements actors = elements.get(0).select("a");
            for (Element actor : actors){
                data.addActors(actor.text());
            }
        }
        // Prime Movie页面
        else {
            // 设置标题
            data.setTitle(document.select("[data-automation-id=\"title\"]").text());
            // 获取导演、演员信息
            Elements elements = document.select("[data-automation-id=\"meta-info\"] > dl");
            // 设置导演
            data.setDirector(elements.get(1).getElementsByClass("a-link-normal").text());
            // 添加演员
            Elements actors = elements.get(2).getElementsByClass("a-link-normal");
            for (Element actor : actors){
                data.addActors(actor.text());
            }
        }
        return data;
    }

    /**
     *
     * @param file 需要解析的文件
     * @return 返回文件内容到字符串中
     */
    public static String ParseFile(File file){
        String html = "";
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            int size = fileInputStream.available();
            byte[] buffer = new byte[size];
            fileInputStream.read(buffer);
            fileInputStream.close();
            html = new String(buffer, "GB2312");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "<html><head></head><body><h>Error</h></body></html>";
        }
        finally {
            return html.toString();
        }
    }
}
