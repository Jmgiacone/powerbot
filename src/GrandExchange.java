import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class GrandExchange {
	public static GEItem getGEItemByID(int itemID) {		
		
		try {			
			URL url = new URL("http://services.runescape.com/m=itemdb_rs/api/catalogue/detail.json?item=" + itemID);
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			
			String line, rawItemDetail = "";
			while((line = br.readLine()) != null) rawItemDetail += line;			
			
			br.close();

            return new GEItem(
                    itemID,//Id of item
                    GEItem.priceToInt(rawItemDetail.replaceFirst(".+\"current\":.+?\"price\":\"?(.+?)\"?}.+", "$1")),//Current Price of item
                    rawItemDetail.replaceFirst(".+\"name\":\"(.+?)\".+", "$1"),//Name of item
                    rawItemDetail.replaceFirst(".+\"description\":\"(.+?)\".+", "$1"),//Description of item
                    rawItemDetail.replaceFirst(".+\"type\":\"(.+?)\".+", "$1"),//Type of item
                    rawItemDetail.replaceFirst(".+\"icon\":\"(.+?)\".+", "$1"),//Small icon
                    rawItemDetail.replaceFirst(".+\"icon_large\":\"(.+?)\".+", "$1"),//Large Icon
                    rawItemDetail.replaceFirst(".+\"members\":\"(.+?)\".+", "$1").equals("true"));//Is a members-only item

		} catch(Exception e) {
			System.out.println("Error while retrieving item details from Grand Exchange: " + e.getMessage());
		}
		
		return null;		
	}
	public static GEItem getGEItemByName(String itemName) {
		try {
			URL url = new URL("http://services.runescape.com/m=itemdb_rs/results.ws");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setDoInput(true);			
			DataOutputStream out = new DataOutputStream(conn.getOutputStream());
			
			out.writeBytes("query=" + URLEncoder.encode(itemName, "UTF-8"));
			out.flush();
			out.close();
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
			String saveFrom = "<table class=\"results\">", saveTill = "</td>", line, content = "";
			
			boolean save = false;			
			while((line = in.readLine()) != null) {		
				if(line.contains(saveFrom))
					save = true;
				
				if(save) 
					content += line;	
				
				if(save && line.contains(saveTill))
					break;
			}
			in.close();

			try {
				Integer.parseInt(content.toLowerCase().replaceFirst(".+obj=(.+?)\">" + itemName.toLowerCase() + "</a>.+", "$1"));
			} catch (Exception e) {
				throw new Exception("Item \""+ itemName +"\" was not found.");
			}
			
			return GrandExchange.getGEItemByID(Integer.parseInt(content.toLowerCase().replaceFirst(".+obj=(.+?)\">" + itemName.toLowerCase() + "</a>.+", "$1")));			
		} catch(Exception e) {
			System.out.println("Error while retrieving an item from the Grand Exchange: " + e.getMessage());
		}
		return null;
	}
}