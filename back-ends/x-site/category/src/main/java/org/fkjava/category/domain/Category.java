package org.fkjava.category.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "category")
@Getter
@Setter
public class Category implements Serializable {

    @Id
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @GeneratedValue(generator = "uuid2")
    private String id;
    private String name;
    @ManyToOne()
    @JoinColumn(name = "parent_id")
    @JsonSerialize(using = ParentCategorySerializer.class)
//    @JsonDeserialize(using =  CategoryDeserializer.class)
    private Category parent;
    @OneToMany(mappedBy = "parent")
    @OrderBy("number")
    @JsonInclude(value = JsonInclude.Include.NON_EMPTY)
    private List<Category> children;
    private Double number = 1.0;

    @Transient
    public boolean isOpen() {
        return children != null && !children.isEmpty();
    }
}
