
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import org.junit.Assert;
import org.junit.Test;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author sfloess
 */
public class ObjectDataMarshallerTest {

    @Test
    public void test_size() {
        final ObjectDataMarshaller data = new ObjectDataMarshaller();

        Assert.assertEquals("Should be -1", -1, data.getDataSize());
    }

    @Test
    public void test_readData() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);

        final String str = "A test string " + System.currentTimeMillis();

        oos.writeObject(str);

        final ObjectDataMarshaller odm = new ObjectDataMarshaller();

        Assert.assertEquals("Should be same string", str, odm.readData(baos.toByteArray()));
    }

    @Test
    public void test_writeData() {
        final String str = "Another test string " + System.currentTimeMillis();

        final ObjectDataMarshaller odm = new ObjectDataMarshaller();

        final byte[] b = odm.writeData(str);

        Assert.assertNotEquals("Should have a size", 0, odm.getDataSize());
        Assert.assertNotNull("Should have serialized", b);
        Assert.assertNotEquals("Should be serialized", 0, b.length);
        Assert.assertEquals("Should be same size", b.length, odm.getDataSize());
    }

    @Test
    public void test_marshall_unmarshall() {
        final String str = "Yet another test string " + System.currentTimeMillis();

        final ObjectDataMarshaller odm = new ObjectDataMarshaller();

        Assert.assertEquals("Should be the same marshalled and unmarshalled", str, odm.readData(odm.writeData(str)));
    }
}
