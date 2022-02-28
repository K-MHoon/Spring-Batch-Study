package com.example.demo.batch.chunk.processor;

import com.example.demo.batch.domain.Product;
import com.example.demo.batch.domain.ProductVO;
import org.modelmapper.ModelMapper;
import org.springframework.batch.item.ItemProcessor;

public class FileItemProcessor implements ItemProcessor<ProductVO, Product> {

    private ModelMapper modelMapper = new ModelMapper();

    @Override
    public Product process(ProductVO item) throws Exception {

        Product product = modelMapper.map(item, Product.class);
        return product;
    }
}
