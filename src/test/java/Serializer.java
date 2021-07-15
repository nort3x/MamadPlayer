import com.google.gson.Gson;
import ir.tesla_tic.model.Meta;
import org.junit.jupiter.api.Test;

public class Serializer {
    @Test void shouldSerializeMeta(){
        Meta m = new Meta("img","salam", Meta.Type.STRING);

        System.out.println(m.toString());
    }
}
