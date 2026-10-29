package edu.irp.portal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecordRepository extends JpaRepository<RepositoryRecord, Long> {

    @Query("""
        select r from RepositoryRecord r
        where (lower(r.title) like lower(concat('%', :q, '%'))
            or lower(r.authors) like lower(concat('%', :q, '%'))
            or lower(r.keywords) like lower(concat('%', :q, '%'))
            or lower(r.department) like lower(concat('%', :q, '%')))
        order by r.updatedAt desc, r.id desc
        """)
    List<RepositoryRecord> search(@Param("q") String q);

    @Query("""
        select r from RepositoryRecord r
        where r.status = :status
          and (lower(r.title) like lower(concat('%', :q, '%'))
            or lower(r.authors) like lower(concat('%', :q, '%'))
            or lower(r.keywords) like lower(concat('%', :q, '%'))
            or lower(r.department) like lower(concat('%', :q, '%')))
        order by r.updatedAt desc, r.id desc
        """)
    List<RepositoryRecord> searchByStatus(@Param("q") String q, @Param("status") RecordStatus status);

    @Query("select r.status, count(r) from RepositoryRecord r group by r.status")
    List<Object[]> countByStatus();

    @Query("select r.department, count(r) from RepositoryRecord r group by r.department")
    List<Object[]> countByDepartment();
}
