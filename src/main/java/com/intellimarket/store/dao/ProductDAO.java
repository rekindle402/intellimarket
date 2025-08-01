package com.intellimarket.store.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.intellimarket.store.domain.Product;
import com.intellimarket.store.domain.ProductStatus;

@Repository
public interface ProductDAO {
	// 전체 목록 조회
	List<Product> selectAll();
	
	// 판매자별 상품 전체 목록 조회
	List<Product> selectById(int sellerId);
	
	// 상품 단 건 조회
	Product select(int productId);
	
	// 상품 등록
	void insert(Product product);
	
	// 상품 정보 수정
	int updateProduct(Product product);
	
	// 상품 상태 변경
	int updateStatus(Product product);
	
	// 상품 삭제
	int delete(int productId);
}
