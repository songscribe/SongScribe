package songscribe.converter;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface ArgumentDescribe {
    String value();
}
