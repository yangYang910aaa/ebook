package com.example.demo.service.impl;

import com.example.demo.common.BusinessException;
import com.example.demo.dto.EbookReq;
import com.example.demo.dto.EbookResp;
import com.example.demo.dto.PageReq;
import com.example.demo.dto.PageResult;
import com.example.demo.entity.Ebook;
import com.example.demo.mapper.EbookMapper;
import com.example.demo.service.EbookService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 电子书服务实现
 */
@Service
public class EbookServiceImpl implements EbookService {

    private static final long MAX_SIZE = 10 * 1024 * 1024L;
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "gif", "png");

    private final EbookMapper ebookMapper;

    @Value("${ebook.cover-dir}")
    private String coverDir;

    @Value("${ebook.cover-url-prefix}")
    private String coverUrlPrefix;

    public EbookServiceImpl(EbookMapper ebookMapper) {
        this.ebookMapper = ebookMapper;
    }

    @Override
    public PageResult<EbookResp> query(String name, Long category2Id, PageReq pageReq) {
        int pageNum = pageReq.getPageNum() == null || pageReq.getPageNum() < 1 ? 1 : pageReq.getPageNum();
        int pageSize = pageReq.getPageSize() == null || pageReq.getPageSize() < 1 ? 10 : pageReq.getPageSize();
        long total = ebookMapper.count(name, category2Id);
        List<EbookResp> list = ebookMapper.selectPage(name, category2Id, (pageNum - 1) * pageSize, pageSize);
        return new PageResult<>(total, list);
    }

    @Override
    public void save(EbookReq req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new BusinessException("电子书名称不能为空");
        }
        if (req.getId() == null) {
            Ebook ebook = new Ebook();
            fill(ebook, req);
            ebookMapper.insert(ebook);
        } else {
            Ebook exist = ebookMapper.selectById(req.getId());
            if (exist == null) {
                throw new BusinessException("电子书不存在");
            }
            Ebook ebook = new Ebook();
            ebook.setId(req.getId());
            fill(ebook, req);
            ebookMapper.update(ebook);
        }
    }

    private void fill(Ebook ebook, EbookReq req) {
        ebook.setName(req.getName());
        ebook.setCategory1Id(req.getCategory1Id() == null ? 0L : req.getCategory1Id());
        ebook.setCategory2Id(req.getCategory2Id() == null ? 0L : req.getCategory2Id());
        ebook.setDescription(req.getDescription());
        ebook.setCover(req.getCover());
    }

    @Override
    public void remove(Long id) {
        ebookMapper.deleteById(id);
    }

    @Override
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的图片");
        }
        String original = file.getOriginalFilename();
        String ext = original == null ? "" : original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException("仅支持 jpg/jpeg/gif/png 格式的图片");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException("图片大小不能超过 10MB");
        }
        File dir = new File(coverDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BusinessException("上传目录创建失败");
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        try {
            file.transferTo(new File(dir, filename));
        } catch (IOException e) {
            throw new BusinessException("图片保存失败");
        }
        return coverUrlPrefix + "/" + filename;
    }
}
