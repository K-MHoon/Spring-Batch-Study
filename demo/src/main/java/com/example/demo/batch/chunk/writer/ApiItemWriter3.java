package com.example.demo.batch.chunk.writer;

import com.example.demo.batch.domain.ApiRequestVO;
import com.example.demo.batch.domain.ApiResponseVO;
import com.example.demo.service.AbstractApiService;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

public class ApiItemWriter3 implements ItemWriter<ApiRequestVO> {
    private final AbstractApiService apiService;

    public ApiItemWriter3(AbstractApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public void write(List<? extends ApiRequestVO> list) throws Exception {
        ApiResponseVO responseVO = apiService.service(list);
        System.out.println("service = " + responseVO);
    }
}
