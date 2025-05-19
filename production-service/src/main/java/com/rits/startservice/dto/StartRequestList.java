package com.rits.startservice.dto;

import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class StartRequestList implements Serializable {
    private List<StartRequest> requestList = new ArrayList<>();
    private List<StartRequestDetails> requestListWithoutBO = new ArrayList<>();
    private String accessToken;

    public StartRequestList deepCopy() {
        try {
            // Serialize the object to a byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(this);

            // Deserialize the byte array to create a deep copy
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (StartRequestList) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // Handle exceptions as needed
            e.printStackTrace();
            return null; // Return null or handle the exception accordingly
        }
    }
}
