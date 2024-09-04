import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class HttpClientTest {
    public static void main(String[] args) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet("http://localhost:5000/hello"); // 确保Python服务器运行在此端口
            String response = EntityUtils.toString(client.execute(request).getEntity());
            System.out.println(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
