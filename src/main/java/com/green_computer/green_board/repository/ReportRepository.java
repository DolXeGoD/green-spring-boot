package com.green_computer.green_board.repository;

import com.green_computer.green_board.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Integer> {
    List<Report> findAllByOrderByIdDesc();
}
