package com.membership.repository;

import com.membership.entity.RechargeStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface RechargeStatsRepository extends JpaRepository<RechargeStats, Long>, JpaSpecificationExecutor<RechargeStats> {

    /**
     * 根据维度查询统计记录
     */
    RechargeStats findByStatDateAndProductIdAndCustomerIdAndSupplierIdAndStatus(
        Date statDate,
        Long productId,
        Long customerId,
        Long supplierId,
        String status
    );

    /**
     * 按产品id、客户id、供应商id、日期排序查询所有统计记录
     */
    List<RechargeStats> findAllByOrderByProductIdAscCustomerIdAscSupplierIdAscStatDateAsc();

    /**
     * 聚合查询：按产品、客户、供应商、状态分组，计算数量和金额总和
     */
    @Query(value = "SELECT MIN(id) as id, MIN(stat_date) as stat_date, product_id, product_name, product_face_price, customer_id, customer_name, supplier_id, supplier_name, status, SUM(recharge_count) as recharge_count, MAX(customer_price) as customer_price, SUM(customer_amount) as customer_amount, MAX(supplier_price) as supplier_price, SUM(cost_amount) as cost_amount, MIN(created_at) as created_at, MIN(updated_at) as updated_at FROM recharge_stats WHERE (:productId IS NULL OR product_id = :productId) AND (:customerId IS NULL OR customer_id = :customerId) AND (:supplierId IS NULL OR supplier_id = :supplierId) AND (:status IS NULL OR status = :status) AND (:startDate IS NULL OR stat_date >= :startDate) AND (:endDate IS NULL OR stat_date <= :endDate) GROUP BY product_id, product_name, product_face_price, customer_id, customer_name, supplier_id, supplier_name, status ORDER BY product_id ASC, customer_id ASC, supplier_id ASC, status ASC", nativeQuery = true)
    List<RechargeStats> findAggregatedStats(
        @Param("productId") Long productId,
        @Param("customerId") Long customerId,
        @Param("supplierId") Long supplierId,
        @Param("status") String status,
        @Param("startDate") Date startDate,
        @Param("endDate") Date endDate
    );

    /**
     * 每日充值汇总查询：按日期、产品、客户、供应商、状态分组，计算数量和金额总和
     */
    @Query(value = "SELECT MIN(id) as id, stat_date, product_id, product_name, product_face_price, customer_id, customer_name, supplier_id, supplier_name, status, SUM(recharge_count) as recharge_count, MAX(customer_price) as customer_price, SUM(customer_amount) as customer_amount, MAX(supplier_price) as supplier_price, SUM(cost_amount) as cost_amount, MIN(created_at) as created_at, MIN(updated_at) as updated_at FROM recharge_stats WHERE (:productId IS NULL OR product_id = :productId) AND (:customerId IS NULL OR customer_id = :customerId) AND (:supplierId IS NULL OR supplier_id = :supplierId) AND (:status IS NULL OR status = :status) AND (:startDate IS NULL OR stat_date >= :startDate) AND (:endDate IS NULL OR stat_date <= :endDate) GROUP BY stat_date, product_id, product_name, product_face_price, customer_id, customer_name, supplier_id, supplier_name, status ORDER BY product_id ASC, customer_id ASC, supplier_id ASC, stat_date DESC, status ASC", nativeQuery = true)
    List<RechargeStats> findDailyStats(
        @Param("productId") Long productId,
        @Param("customerId") Long customerId,
        @Param("supplierId") Long supplierId,
        @Param("status") String status,
        @Param("startDate") Date startDate,
        @Param("endDate") Date endDate
    );
}