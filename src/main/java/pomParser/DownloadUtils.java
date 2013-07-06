package pomParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.CharBuffer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


public class DownloadUtils {
	
	private static String mavenRepoURL = "http://search.maven.org/remotecontent?filepath=";
	private static String mavenAPISearch = "http://search.maven.org/solrsearch/select?q=g:<insert>&rows=1&wt=xml";
	
	public static boolean downloadFileFromCentralRepo(String query,String type,String saveTo){
	        URL url;
	        DownloaderProxyAuthenticator.setupProxy();
	        try {
	        	System.err.println(mavenRepoURL+query+"."+type);
	            url = new URL(mavenRepoURL+query+"."+type);
	            int httpCode = checkIfValidURL(url);
	            if(httpCode != 200){
	            	//TODO log a mesasge saying that the file could be downloaded
	            	return false;
	            }else{
	            	//TODO log downloaded successfully
	            	System.err.println("downloaded successfully");
	            }
	            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
	            new File(saveTo.substring(0,saveTo.lastIndexOf("\\"))).mkdirs();
	            File file = new File(saveTo);
	            file.setWritable(true);
	            BufferedWriter out = new BufferedWriter(new FileWriter(saveTo));
	            CharBuffer cbuf = CharBuffer.allocate(255);
	            while ((in.read(cbuf)) != -1) {
	                out.append((CharSequence) cbuf.flip());
	                cbuf.clear();
	            }
	            in.close();
	            out.close();
	        } catch (MalformedURLException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        return true;
	}

	public static String checkMavenForLatestVersion(Dependency dep) throws MalformedURLException{
        Document result = null;
		try {
			result = Jsoup.parse(new URL(mavenAPISearch.replace("<insert>", dep.getGroupId())), 1000);
		} catch (IOException e) {
			//TODO log error display message
		}
		//TODO check to see if result is null (the connection is null)
		
        return result.select("str[name=latestVersion]").html();
}
	private static int checkIfValidURL(URL url) throws IOException{
		HttpURLConnection con =  ( HttpURLConnection )  url.openConnection (); 
		con.setRequestMethod ("GET"); 
		con.connect () ; 
		int code = con.getResponseCode() ;
		return code;
		
	}
	public static void createDownloader(String query,boolean blocking,String type,String saveTo){
		if(blocking){
			downloadFileFromCentralRepo(query,type,saveTo);
		}else{
			new Thread(new Downloader(query,type,saveTo)).start();
		}
	}
	
	
}
