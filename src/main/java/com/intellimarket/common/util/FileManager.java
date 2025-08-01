package com.intellimarket.common.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.intellimarket.admin.domain.Banner;
import com.intellimarket.common.exception.CommonException;
import com.intellimarket.store.domain.Product;
import com.intellimarket.store.domain.ProductImage;
import com.intellimarket.store.domain.StoreInfo;

import lombok.extern.slf4j.Slf4j;

//이 객체의 존재가 없다면 , 컨트롤러가 '업로드' 라는 모델 영역의 업무를 수행하게 되므로
//업로드 수행을 전담할 수 있는 모델객체를 정의한다.
//이 객체는 , DAO도 아니며 , Service도 아니며 , Controller도 아니므로 , 스프링의 기본 컴포넌트로 등록

@Slf4j
@Component //ComponentScan의 대상이 될 수 있다. 따라서 자동으로 인스턴스가 올라온다
public class FileManager {
	public void save(Product product, String path, String prefix) throws CommonException{
		
		//파일의 수가 복수개 이므로, 상품마다 1:1 대응하는 디렉토리를 생성하자
		File directory = new File(path,prefix+"_"+product.getProductId());
		
		//MultipartFile 변수와 html 이름이 동일하면 매핑됨
		MultipartFile[] photo = product.getPhoto();
		if (photo == null || photo.length == 0 || photo[0].isEmpty()) return;
		List imgList = new ArrayList();
		
		for(int i=0;i<photo.length;i++) {
			//메모리 상의 파일 정보가 , 실제 디스크상으로 존재하게 되는 시점 !!
			try {
				log.debug("원본 파일명은" + photo[i].getOriginalFilename());
				
				String ori = photo[i].getOriginalFilename();
				String ext = ori.substring(ori.lastIndexOf(".")+1,ori.length());
				
				//개발자가 원하는 파일명 생성하기
				try {
					Thread.sleep(10);
					//연산속도가 너무 빠르면 파일명이 중복될 수 있다.
					//일부러 지연 !! 
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//상품명 + i+1 . 확장자 로 파일명 지정
				//Ex) 개껌 썸네일 3개 저장 : 개껌_1.png / 개껌_2.png / 개껌_3.png 
				String firstname = product.getProductName() + "_" + (i + 1);
				String filename = firstname+"."+ext;
				
				ProductImage productImage = new ProductImage();
				productImage.setFilename(filename);
				imgList.add(productImage);
				product.setImgList(imgList);
				//realPath를 사용하려면 , 앱의 전반적인 전역적 정보를 가진 객체인 ServletContext가 필요함 ! 
				
				File file = new File(directory.getAbsolutePath()+File.separator+filename);
				log.debug("업로드된 이미지가 생성된 경로는 "+path);
				photo[i].transferTo(file);
				
			} catch (Exception e) {
				e.printStackTrace();
				throw new CommonException("파일 업로드 실패",e);
			} 
			
		}
	}

	//상품 이미지 삭제 (지정한 상품의 디렉토리 및 그 안의 파일들)
	//savePath ~~~/data/p_pk값
	public void remove(Product product,String savePath, String prefix) {

		// 디렉토리를 지우기 위해서는 , 그 안에 파일들이 먼저 지워져야 함
		//1) 조사 대상 디렉토리를 지정
		File directory = new File(savePath, prefix+"_"+product.getProductId());
		//디렉토리가 실제로 존재할 경우 그 안의 파일부터 지우기
		
		if(directory.exists() && directory.isDirectory()) {
			//이 하위에 파일들이 존재하는지 그 목록을 얻기
			File[] files = directory.listFiles();
			
			if(files !=null) { //파일이 존재한다면
				//파일의 수만큼 삭제
				
				for(File file : files) {
					boolean deleted = file.delete();
					log.debug(file.getName()+"를 삭제한 결과 "+deleted);
				}
			}
			
			//파일이 모두 삭제되었으므로 , 디렉토리도 삭제
			boolean result = directory.delete();
			if(result == false) {
				log.warn("디렉토리 삭제 실패" + directory.getAbsolutePath());
			}
		}
		
	}
	
	// 쇼핑몰 관리자 배너 이미지 저장
	public void save(Banner banner, String path) throws CommonException{
		MultipartFile imageFile = banner.getImageFile();
		
		if(imageFile == null || imageFile.isEmpty()) throw new CommonException("배너 이미지 파일이 없습니다.");
		
		try {
			String ori = imageFile.getOriginalFilename();
	        String ext = ori.substring(ori.lastIndexOf(".") + 1);
	        String fileName = System.currentTimeMillis() + "." + ext;
			
	        File file = new File(path, fileName);
	        imageFile.transferTo(file);
			
	        // DB에 저장할 경로 세팅
	        banner.setImagePath("/resources/common/img/banner/" + fileName);
		} catch (Exception e) {
			throw new CommonException("배너 이미지 저장 중 오류가 발생했습니다.", e);
		} 
	}
	
	// 스토어 판매자 로고 이미지 저장
	public void save(StoreInfo storeInfo, String path) throws CommonException{
		MultipartFile imageFile = storeInfo.getImageFile();
		
		if(imageFile == null || imageFile.isEmpty()) throw new CommonException("로고 이미지 파일이 없습니다.");
		
		try {
			String ori = imageFile.getOriginalFilename();
	        String ext = ori.substring(ori.lastIndexOf(".") + 1);
	        String fileName = System.currentTimeMillis() + "." + ext;
			
	        File file = new File(path, fileName);
	        imageFile.transferTo(file);
			
	        // DB에 저장할 경로 세팅
	        storeInfo.setLogoPath("/resources/common/img/logo/" + fileName);
			log.debug("업로드된 이미지가 생성된 경로는 "+path);
		} catch (Exception e) {
			throw new CommonException("배너 이미지 저장 중 오류가 발생했습니다.", e);
		} 
	}
	
	
	// 쇼핑몰 관리자 배너 이미지 삭제
	public void remove(Banner banner, String savePath) {
		String imgPath = banner.getImagePath(); // /resources/common/img/banner/파일명.png

		if(imgPath != null) {
			String fileName = imgPath.substring(imgPath.lastIndexOf("/")+1);
			File file = new File(savePath, fileName);
			
			if(file.exists()) {
				boolean deleted = file.delete();
				log.debug(file.getAbsolutePath() + " 삭제 결과: " + deleted);
			}
		}
	}
}