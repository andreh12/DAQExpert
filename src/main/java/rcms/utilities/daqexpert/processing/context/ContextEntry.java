package rcms.utilities.daqexpert.processing.context;

import javax.persistence.*;
import java.io.Serializable;
import java.util.stream.Collectors;

//@MappedSuperclass
@Entity
@Inheritance(strategy= InheritanceType.JOINED)
@Table(name="condition_context")
public abstract class ContextEntry <T> implements Serializable {


    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    Long id;

    @Column(length = 1)
    String type;

    public abstract String getTextRepresentation();

    public abstract T getValue();
}
