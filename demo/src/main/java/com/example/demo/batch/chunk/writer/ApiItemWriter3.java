package com.example.demo.batch.chunk.writer;

import com.example.demo.batch.domain.ApiRequestVO;
import com.example.demo.batch.domain.ApiResponseVO;
import com.example.demo.service.AbstractApiService;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.core.io.FileSystemResource;

import java.util.List;

public class ApiItemWriter3 extends FlatFileItemWriter<ApiRequestVO> {
    private final AbstractApiService apiService;

    public ApiItemWriter3(AbstractApiService apiService) {
        this.apiService = apiService;
    }

    @Override
    public void write(List<? extends ApiRequestVO> list) throws Exception {
        ApiResponseVO responseVO = apiService.service(list);
        System.out.println("service = " + responseVO);

        list.forEach(item -> item.setApiResponseVO(responseVO));

        super.setResource(new FileSystemResource("C:\\Users\\MHK\\Desktop\\SpringBatch\\demo\\src\\main\\resources\\product3.txt"));
        super.open(new ExecutionContext());
        super.setLineAggregator(new DelimitedLineAggregator<>());
        super.setAppendAllowed(true);
        super.write(list);
    }
}
