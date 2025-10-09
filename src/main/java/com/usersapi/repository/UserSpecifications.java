package com.usersapi.repository;

import com.usersapi.model.Gender;
import com.usersapi.model.User;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecifications {

    public static Specification<User> firstNameContains(String q) {
        return (root, query, cb) -> q == null ? null :
                cb.like(cb.lower(root.get("firstName")), "%" + q.toLowerCase() + "%");
    }

    public static Specification<User> lastNameContains(String q) {
        return (root, query, cb) -> q == null ? null :
                cb.like(cb.lower(root.get("lastName")), "%" + q.toLowerCase() + "%");
    }

    public static Specification<User> emailContains(String q) {
        return (root, query, cb) -> q == null ? null :
                cb.like(cb.lower(root.get("email")), "%" + q.toLowerCase() + "%");
    }

    public static Specification<User> genderEquals(Gender gender) {
        return (root, query, cb) -> gender == null ? null :
                cb.equal(root.get("gender"), gender);
    }

    public static Specification<User> phoneBrandContains(String q) {
        return (root, query, cb) -> {
            if (q == null) return null;
            var join = root.join("phone", JoinType.LEFT);
            query.distinct(true);
            return cb.like(cb.lower(join.get("brand")), "%" + q.toLowerCase() + "%");
        };
    }

    public static Specification<User> phoneNumberContains(String q) {
        return (root, query, cb) -> {
            if (q == null) return null;
            var join = root.join("phone", JoinType.LEFT);
            query.distinct(true);
            return cb.like(cb.lower(join.get("number")), "%" + q.toLowerCase() + "%");
        };
    }
}