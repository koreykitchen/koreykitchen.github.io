import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Scanner; 
import org.json.*;

public class MileageTracker 
{
	private double totalMiles;
	private String previousStoreLocation;

	public MileageTracker() 
	{
		totalMiles = 0;
		previousStoreLocation = null;
	}

	public double getTotalMiles()
	{
		return totalMiles;
	}

	public void calculateMileage(String storeLocation)
	{
		//If this is not the first store
		if(previousStoreLocation != null)
		{
			double miles = 0;

			String urlString = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" +
							previousStoreLocation + "&destinations=" + storeLocation;
			
			urlString = urlString.replaceAll(" ","+");

			try
			{
				URL url = new URL(urlString);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				
				BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				
				String inputLine;
				StringBuffer response = new StringBuffer();

				while ((inputLine = inputBuffer.readLine()) != null) 
				{
					response.append(inputLine);
				}
				
				inputBuffer.close();

				//Need to parse the JSON
				JSONObject responseObject = new JSONObject(response.toString());
				
				JSONArray rowsArray = responseObject.getJSONArray("rows");
			    JSONObject object1 = rowsArray.getJSONObject(0);
			    JSONArray elementsArray = object1.getJSONArray("elements");
			    JSONObject object2 = elementsArray.getJSONObject(0);
			    JSONObject object3 = object2.getJSONObject("distance");
			    int meters = object3.getInt("value");
			    
			    miles = meters / 1609.34;

			}

			catch(Exception e)
			{
				System.out.print('\n' + "ERROR: Mileage not added..." + '\n');
				
				System.out.print("\n" + e + '\n');
			}

			totalMiles += miles;
		}

		previousStoreLocation = storeLocation;
	}

	public static void main(String[] args) 
	{
		MileageTracker miles = new MileageTracker();

		Scanner scanner = new Scanner(System.in);

		String input;

		//How to break out of loop message
		System.out.print('\n' + "Break out of loop by typing \"quit\"..." + '\n');

		//Input first store
		System.out.print("\n" + "Enter first store's address: ");
		input = scanner.nextLine();

		while(!input.equals("quit"))
		{
			//Calculate Mileage
			miles.calculateMileage(input);

			//Output Total Miles
			System.out.print('\n' + "Total Miles: " + new DecimalFormat(".##").format(miles.getTotalMiles()) + '\n');

			//Input Next Store
			System.out.print('\n' + "Enter next store's address: ");
			input = scanner.nextLine();
		}

		scanner.close();
	}
}
