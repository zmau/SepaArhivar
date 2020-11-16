package net.zmau.sepaarchive.dal;

import javax.persistence.*;

@Entity
public class Station {
    @Id
    @GeneratedValue
    private Long id;

    @Column
    private Integer sepaId;

    @Column(length = 32, unique = true)
    private String name;


    public long getId() {
        return id;
    }

    public long getSepaId() {
        return sepaId;
    }
    public void setSepaId(int sepaId) {
        this.sepaId = sepaId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
