package com.example.gradox2.service.implementation;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.gradox2.persistence.repository.FileRepository;
import com.example.gradox2.persistence.repository.UserRepository;
import com.example.gradox2.presentation.dto.stats.PlatformStatsResponse;
import com.example.gradox2.service.interfaces.IStatsService;

@Service
public class StatsServiceImpl implements IStatsService {

    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    public StatsServiceImpl(FileRepository fileRepository, UserRepository userRepository) {
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PlatformStatsResponse getPlatformStats() {
        return PlatformStatsResponse.builder()
                .totalFiles(fileRepository.count())
                .totalUsers(userRepository.count())
                .totalStorageBytes(fileRepository.sumSizeBytes())
                .totalDownloads(fileRepository.sumDownloadCount())
                .byType(fileRepository.countByType().stream()
                        .map(stat -> PlatformStatsResponse.TypeStat.builder()
                                .type(stat.getType())
                                .count(stat.getCount())
                                .build())
                        .collect(Collectors.toList()))
                .bySubject(fileRepository.countBySubject().stream()
                        .map(stat -> PlatformStatsResponse.SubjectStat.builder()
                                .code(stat.getCode())
                                .name(stat.getName())
                                .courseName(stat.getCourseName())
                                .count(stat.getCount())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}