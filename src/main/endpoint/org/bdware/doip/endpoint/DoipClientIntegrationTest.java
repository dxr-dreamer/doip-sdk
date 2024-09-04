package org.bdware.doip.endpoint;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bdware.doip.codec.digitalObject.DigitalObject;
import org.bdware.doip.codec.digitalObject.DoType;
import org.bdware.doip.endpoint.client.ClientConfig;
import org.bdware.doip.endpoint.client.DoipClientImpl;

public class DoipClientIntegrationTest {

    public static void main(String[] args) throws InterruptedException {
        String targetDoipService = "udp://127.0.0.1:8004/";
        DoipClientImpl client = new DoipClientImpl();
        ClientConfig config = ClientConfig.fromUrl(targetDoipService);
        client.connect(config);

        // Hello
        client.hello("TestService", msg -> {
            System.out.println("Hello Response: " + msg.body.getDataAsJsonString());
        });
        Thread.sleep(2000); // wait for the response

        // List Operations
        client.listOperations("TestService", msg -> {
            System.out.println("List Operations Response: " + msg.body.getDataAsJsonString());
        });
        Thread.sleep(2000);
        // Create
        DigitalObject digitalObject = new DigitalObject("newObject", DoType.DO);
        digitalObject.addAttribute("description", "A test digital object.");
        client.create(targetDoipService, digitalObject, createMsg -> {
            String createResponse = createMsg.body.getDataAsJsonString();
            System.out.println("Create Response: " + createResponse);

            try {
                String doid = extractDoid(createResponse); // Parse the DOID from the create response
                if (doid == null || doid.isEmpty()) {
                    throw new IllegalStateException("Failed to extract DOID from create response");
                }

                digitalObject.id = doid; // Set the DOID for subsequent operations

                // First retrieval of the digital object using the newly created DOID
                client.retrieve(doid, "", false, firstRetrieveMsg -> {
                    try {
                        DigitalObject firstRetrievedDO = firstRetrieveMsg.body.getDataAsDigitalObject();
                        System.out.println("First Retrieve Response: " + new Gson().toJson(firstRetrievedDO));

                        // Update the digital object
                        firstRetrievedDO.addAttribute("description", "Updated description.");
                        client.update(firstRetrievedDO, updateMsg -> {
                            System.out.println("Update Response: " + updateMsg.body.getDataAsJsonString());

                            // Second retrieval post update
                            client.retrieve(doid, "", false, secondRetrieveMsg -> {
                                try {
                                    DigitalObject secondRetrievedDO = secondRetrieveMsg.body.getDataAsDigitalObject();
                                    System.out.println("Second Retrieve Response: " + new Gson().toJson(secondRetrievedDO));

                                    // Delete the digital object
                                    client.delete(doid, deleteMsg -> {
                                        System.out.println("Delete Response: " + deleteMsg.body.getDataAsJsonString());
                                    });
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    System.err.println("Error during second retrieval: " + e.getMessage());
                                }
                            });
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Error during first retrieval or update: " + e.getMessage());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error processing create response: " + e.getMessage());
            }
        });



        // 给一点时间确保所有操作完成
        Thread.sleep(10000);
    }



    static private String extractDoid(String jsonResponse) {
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
        return jsonObject.get("doid").getAsString();
    }



}
