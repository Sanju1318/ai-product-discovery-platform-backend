package com.example.ProductRecomdation.Service;

import com.example.ProductRecomdation.Model.Product;
import com.example.ProductRecomdation.Model.ProductData;
import com.example.ProductRecomdation.Model.ProductMatch;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.json.JsonMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
@Service
public class ProductService {
    private final EmbeddingModel embeddingModel;
    private final JsonMapper jsonMapper;

    private final List<Product> productsEmbedding = new ArrayList<>();

    public ProductService(
            EmbeddingModel embeddingModel,
            JsonMapper jsonMapper) {

        this.embeddingModel = embeddingModel;
        this.jsonMapper = jsonMapper;
    }

    @PostConstruct
    public void initializeProducts() throws IOException {
        System.out.println("========== PRODUCT INITIALIZATION START ==========");
        ClassPathResource resource = new ClassPathResource("products.json");

        System.out.println("JSON exists: " + resource.exists());
        InputStream inputStream = resource.getInputStream();

        List<ProductData> productDataList = jsonMapper.readValue(inputStream, new TypeReference<List<ProductData>>() {});

        for (ProductData productData : productDataList) {
            System.out.println(
                    "Loading: " + productData.getTitle()
            );
            float[] embedding=embeddingModel.embed(
                    productData.getDescription()
            );

            Product product = new Product(
                    productData.getTitle(),
                    productData.getDescription(),
                    embedding
            );
            productsEmbedding.add(product);
        }
        inputStream.close();

        for(Product product: productsEmbedding) {
            System.out.println(Arrays.toString(product.getEmbedding()));
        }

        System.out.println(
                "productsEmbedding size: "
                        + productsEmbedding.size()
        );

        System.out.println("========== PRODUCT INITIALIZATION END ==========");

    }

    // Search products based on user query
    public List<ProductMatch> search(String query) {
        float[] userQueryEmbedding=embeddingModel.embed(query);
        List<ProductMatch> matches = new ArrayList<>();
        for (Product product : productsEmbedding) {
            double similarity= cosineSimilarity(userQueryEmbedding,product.getEmbedding());

            ProductMatch match = new ProductMatch(product.getTitle(), product.getDescription(), similarity);
            matches.add(match);
        }
        sortBySimilarity(matches);
        return topKMatches(matches,5);
    }

    //Similar products
    public List<ProductMatch> similarProduct(String title) {
        Product selectedProduct=findProduct(title);
        List<ProductMatch> matches = new ArrayList<>();
        for (Product product : productsEmbedding) {
            if (product.getTitle()
                    .equalsIgnoreCase(title.trim())) {

                    continue;
            }
            double similarity=cosineSimilarity(selectedProduct.getEmbedding(),
                    product.getEmbedding());

            ProductMatch match = new ProductMatch(product.getTitle(),
                    product.getDescription(), similarity);
            matches.add(match);


        }
        sortBySimilarity(matches);
        return topKMatches(matches,5);
    }

    private Product findProduct(String title) {
        System.out.println("========== FIND PRODUCT ==========");
        System.out.println("Searching for: [" + title + "]");
        for (Product product : productsEmbedding) {
            System.out.println(
                    "Checking: [" + product.getTitle() + "]"
            );

            if (product.getTitle()
                    .equalsIgnoreCase(title.trim())) {

                System.out.println(
                        "FOUND: [" + product.getTitle() + "]"
                );

                return product;
            }
        }

        System.out.println(
                "NOT FOUND: [" + title + "]"
        );

        throw new IllegalArgumentException(
                "Product not found: " + title
        );



    }
    private double cosineSimilarity(
            float[] a,
            float[] b) {

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {

            dotProduct += a[i] * b[i];

            normA += a[i] * a[i];

            normB += b[i] * b[i];
        }

        if (normA == 0 || normB == 0) {
            return 0.0;
        }

        return dotProduct /
                (Math.sqrt(normA) * Math.sqrt(normB));
    }


    private void sortBySimilarity(List<ProductMatch> matches){

        Collections.sort(matches,(first,second)->
                Double.compare(
                        second.getMatch(),
                        first.getMatch())
                );

    }

    private List<ProductMatch> topKMatches(List<ProductMatch> matches, int limit) {

        List<ProductMatch> topKMatches = new ArrayList<>();
        int numberOfMatches = Math.min(limit, matches.size());
        for (int i = 0; i < numberOfMatches; i++) {
            topKMatches.add(matches.get(i));
        }
        return topKMatches;
    }

}
