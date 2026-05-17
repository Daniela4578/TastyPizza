package objects;

import java.util.Objects;

public class Category {

    private final Long id;
    private final String name;

    public Category(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
    }

    public static Builder builder() { return new Builder(); }

    public Long getId() {return id;}
    public String getName() {return name;}

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category category)) return false;
        return Objects.equals(id, category.id);
    }

    @Override public int hashCode() {
        return Objects.hash(id);
    }

    @Override public String toString() {
        return String.format("Category[id: %d, name: '%s']", id, name);
    }

    public static class Builder {
        private Long id;
        private String name;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Category build() {
            Objects.requireNonNull(name, "Category name is required");
            return new Category(this);
        }
    }
}
