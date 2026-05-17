package com.thacbao.orderservice.repository;

import com.thacbao.common.enums.OrderStatus;
import com.thacbao.orderservice.dto.request.OrderFilterRequest;
import com.thacbao.orderservice.model.Order;
import com.thacbao.orderservice.model.OrderItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Order> filterOrders(OrderFilterRequest filter, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> root = cq.from(Order.class);

        List<Predicate> predicates = buildFilterPredicates(cb, root, filter);
        cq.where(predicates.toArray(new Predicate[0]));

        // Sorting
        List<jakarta.persistence.criteria.Order> orders = buildSortOrders(cb, root, pageable.getSort());
        if (orders.isEmpty()) {
            orders.add(cb.desc(root.get("createdAt")));
        }
        cq.orderBy(orders);

        // Count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Order> countRoot = countQuery.from(Order.class);
        countQuery.select(cb.count(countRoot));
        List<Predicate> countPredicates = buildFilterPredicates(cb, countRoot, filter);
        countQuery.where(countPredicates.toArray(new Predicate[0]));
        long total = entityManager.createQuery(countQuery).getSingleResult();

        // Data query with pagination
        TypedQuery<Order> typedQuery = entityManager.createQuery(cq);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        return new PageImpl<>(typedQuery.getResultList(), pageable, total);
    }

    private List<Predicate> buildFilterPredicates(CriteriaBuilder cb, Root<Order> root, OrderFilterRequest filter) {
        List<Predicate> predicates = new ArrayList<>();

        if (StringUtils.hasText(filter.getKeyword())) {
            String keyword = "%" + filter.getKeyword().toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("orderNumber")), keyword),
                    cb.like(cb.lower(root.get("userEmail")), keyword),
                    cb.like(cb.lower(root.get("userFullName")), keyword)
            ));
        }

        if (filter.getUserId() != null) {
            predicates.add(cb.equal(root.get("userId"), filter.getUserId()));
        }

        if (filter.getMinAmount() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("finalAmount"), filter.getMinAmount()));
        }
        if (filter.getMaxAmount() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("finalAmount"), filter.getMaxAmount()));
        }

        if (filter.getStartDate() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"),
                    filter.getStartDate().atStartOfDay()));
        }
        if (filter.getEndDate() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"),
                    filter.getEndDate().atTime(LocalTime.MAX)));
        }

        if (StringUtils.hasText(filter.getDistrict())) {
            predicates.add(cb.equal(root.get("district"), filter.getDistrict()));
        }
        if (StringUtils.hasText(filter.getWard())) {
            predicates.add(cb.equal(root.get("ward"), filter.getWard()));
        }

        if (StringUtils.hasText(filter.getStatus())) {
            predicates.add(cb.equal(root.get("status"), OrderStatus.valueOf(filter.getStatus().toUpperCase())));
        }

        return predicates;
    }

    @Override
    public Page<Order> findByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> root = cq.from(Order.class);

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        cq.where(cb.between(root.get("createdAt"), start, end));
        cq.orderBy(cb.desc(root.get("createdAt")));

        long total = entityManager.createQuery(
                cb.createQuery(Long.class).select(cb.count(cb.createQuery(Long.class).from(Order.class).get("id")))
        ).getSingleResult();

        TypedQuery<Order> query = entityManager.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        return new PageImpl<>(query.getResultList(), pageable, total);
    }

    @Override
    public Page<Order> findByDistrict(String district, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> root = cq.from(Order.class);

        cq.where(cb.equal(cb.lower(root.get("district")), district.toLowerCase()));
        cq.orderBy(cb.desc(root.get("createdAt")));

        TypedQuery<Order> query = entityManager.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Order> countRoot = countQuery.from(Order.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(cb.equal(cb.lower(countRoot.get("district")), district.toLowerCase()));
        long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(query.getResultList(), pageable, total);
    }

    @Override
    public List<Object[]> getDailyOrderCounts(LocalDate startDate, LocalDate endDate) {
        String jpql = """
            SELECT FUNCTION('DATE', o.createdAt) as orderDate, COUNT(o)
            FROM Order o
            WHERE o.createdAt BETWEEN :start AND :end
              AND o.status <> :cancelled
            GROUP BY FUNCTION('DATE', o.createdAt)
            ORDER BY FUNCTION('DATE', o.createdAt) ASC
            """;
        return entityManager.createQuery(jpql, Object[].class)
                .setParameter("start", startDate.atStartOfDay())
                .setParameter("end", endDate.atTime(LocalTime.MAX))
                .setParameter("cancelled", OrderStatus.CANCELLED)
                .getResultList();
    }

    @Override
    public List<Object[]> getDailyRevenue(LocalDate startDate, LocalDate endDate) {
        String jpql = """
            SELECT FUNCTION('DATE', o.createdAt) as orderDate, COALESCE(SUM(o.finalAmount), 0)
            FROM Order o
            WHERE o.createdAt BETWEEN :start AND :end
              AND o.status <> :cancelled
            GROUP BY FUNCTION('DATE', o.createdAt)
            ORDER BY FUNCTION('DATE', o.createdAt) ASC
            """;
        return entityManager.createQuery(jpql, Object[].class)
                .setParameter("start", startDate.atStartOfDay())
                .setParameter("end", endDate.atTime(LocalTime.MAX))
                .setParameter("cancelled", OrderStatus.CANCELLED)
                .getResultList();
    }

    @Override
    public BigDecimal getTotalRevenueByUser(Integer userId) {
        String jpql = "SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.userId = :userId AND o.status <> :cancelled";
        return entityManager.createQuery(jpql, BigDecimal.class)
                .setParameter("userId", userId)
                .setParameter("cancelled", OrderStatus.CANCELLED)
                .getSingleResult();
    }

    @Override
    public List<Object[]> getTopDistrictsByOrderCount(int limit) {
        String jpql = """
            SELECT o.district, COUNT(o)
            FROM Order o
            GROUP BY o.district
            ORDER BY COUNT(o) DESC
            """;
        return entityManager.createQuery(jpql, Object[].class)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public List<Object[]> getTopSellingProducts(int limit) {
        String jpql = """
            SELECT oi.productId, oi.productName, SUM(oi.quantity)
            FROM OrderItem oi
            JOIN oi.order o
            WHERE o.status <> :cancelled
            GROUP BY oi.productId, oi.productName
            ORDER BY SUM(oi.quantity) DESC
            """;
        return entityManager.createQuery(jpql, Object[].class)
                .setParameter("cancelled", OrderStatus.CANCELLED)
                .setMaxResults(limit)
                .getResultList();
    }

    @Override
    public List<Object[]> getOrderStatusDistribution() {
        String jpql = """
            SELECT o.status, COUNT(o)
            FROM Order o
            GROUP BY o.status
            ORDER BY COUNT(o) DESC
            """;
        return entityManager.createQuery(jpql, Object[].class)
                .getResultList();
    }

    private List<jakarta.persistence.criteria.Order> buildSortOrders(CriteriaBuilder cb, Root<Order> root, Sort sort) {
        List<jakarta.persistence.criteria.Order> orders = new ArrayList<>();
        for (Sort.Order sortOrder : sort) {
            Path<?> path = root.get(sortOrder.getProperty());
            orders.add(sortOrder.isAscending() ? cb.asc(path) : cb.desc(path));
        }
        return orders;
    }
}
