package com.tcmdiff.SpringTcmDiff.repository;

import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tcmdiff.SpringTcmDiff.models.DiffResult;

@Repository
public interface DiffResultRepository extends JpaRepository<DiffResult,Long>{ //實例DiffResult class產生 類型Long
	//用JPQL 呼叫Entity DiffResult也就是class，:identity佔位對應方法參數上@Param("identity")，DESC遞減
	@Query("SELECT d FROM DiffResult d WHERE d.identity = :identity ORDER BY d.dateTime DESC")
	List<DiffResult> findLatestDiffByDateTime(@Param("identity") String identity);

}
