package pomParser;

public class Downloader implements Runnable {
	String query = "";
	String type = "";
	String saveTo = "";
	@Override
	public void run() {
		DownloadUtils.downloadFileFromCentralRepo(this.query, this.type,this.saveTo);
	}

	Downloader(String query,String type,String saveTo){
		this.query = query;
		this.type = type;
		this.saveTo = saveTo;
	}

}
