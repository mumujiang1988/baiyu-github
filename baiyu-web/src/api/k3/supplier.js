import request from '@/utils/request'

/**
 * 获取供应商分页列表
 * @param {query} query 查询参数
 * @returns {queryParams}
 */
export function listsupplier(queryParams) {
  return request({
    url: '/k3/supplier/page',
    method: 'get',
    params: queryParams
  })
}

/**
 * 新增供应商
 * @param {Object} data 供应商数据
 * @returns
 */
export function addsupplier(data) {
  const formData = new FormData();

  // 分离营业执照文件和其他数据
  const { businessLicense, businessLicenseFile, ...supplierData } = data;

  // 将其他数据转换为JSON
  const dataBlob = new Blob([JSON.stringify(supplierData)], { type: 'application/json' });
  formData.append('data', dataBlob);

  // 如果有营业执照文件，单独添加
  if (businessLicenseFile && businessLicenseFile instanceof File) {
    formData.append('image', businessLicenseFile);
  }

  // 处理回访信息附件（如果存在）
  if (supplierData.supplierVisitRecord && supplierData.supplierVisitRecord.length > 0) {
    // 查找有附件的回访记录
    const visitRecordWithFile = supplierData.supplierVisitRecord.find(
      record => record.attachmentFile && record.attachmentFile instanceof File
    );

    if (visitRecordWithFile && visitRecordWithFile.attachmentFile) {
      formData.append('visitFile', visitRecordWithFile.attachmentFile);
    }
  }

  return request({
    url: '/k3/supplier/save',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 修改供应商
 * @param {Object} data 供应商数据
 * @returns
 */
export function updatesupplier(data) {
  const formData = new FormData();

  // 分离营业执照文件和其他数据
  const { businessLicense, businessLicenseFile, ...supplierData } = data;

  // 将其他数据转换为JSON
  const dataBlob = new Blob([JSON.stringify(supplierData)], { type: 'application/json' });
  formData.append('data', dataBlob);

  // 如果有营业执照文件，单独添加
  if (businessLicenseFile && businessLicenseFile instanceof File) {
    formData.append('image', businessLicenseFile);
  }

  // 处理回访信息附件（如果存在）
  if (supplierData.supplierVisitRecord && supplierData.supplierVisitRecord.length > 0) {
    // 查找有附件的回访记录（只处理新上传的附件）
    const visitRecordWithFile = supplierData.supplierVisitRecord.find(
      record => record.attachmentFile && record.attachmentFile instanceof File
    );

    if (visitRecordWithFile && visitRecordWithFile.attachmentFile) {
      formData.append('visitFile', visitRecordWithFile.attachmentFile);
    }
  }

  return request({
    url: '/k3/supplier/update',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除供应商
 * @param {Array} number 供应商ID数组
 * @returns
 */
export function deletesupplier(numbers) {
  return request({
    url: '/k3/supplier/delete/' +numbers,
    method: 'delete'
  })
}

/**
 * 获取供应商详情
 * @param {String} id 供应商ID
 * @returns
 */
export function getsupplier(number) {
  return request({
    url: `/k3/supplier/query`,
    method: 'get',
    params: { number }
  })
}

/**
 * 国家
 * */
export function getnAtion(){
  return request({
    url: `/api/v1/kingdee/getnAtion`,
    method: 'get'
  })
}


/**
 * 获取字典选项
 * @param {String} type 字典类型
 * @returns
 */
export function getDictOptions(type) {
  const urls = {
    /*供应商分类*/
    supplierCategory: '/api/v1/kingdee/supplierclassification',
    /* 供应商来源 */
    source: '/api/v1/kingdee/suppliersource',
    /*查询负责人*/
    manager: '/api/v1/kingdee/user/all',
    /*币别*/
    settlementCurrency: '/api/v1/kingdee/currency',
    /*结算方式*/
    settlementMethod: '/api/v1/kingdee/settlementMethod/all',
    /*付款条件*/
    paymentTerms: '/api/v1/kingdee/paymentterms',
    /*发票类型*/
    invoiceType: '/api/v1/kingdee/invoicetype',
    /*税分类*/
    taxCategory: '/api/v1/kingdee/taxclassification',
    /*税率*/
    defaultTaxRate: '/api/v1/kingdee/taxRate/all',
    /*供应类别*/
    supplyType: '/api/v1/kingdee/supplycategory'
  };
  const url = urls[type] || `/api/v1/kingdee/${type}`;

  return request({
    url: url,
    method: 'get'
  });
}

/**
 * 供应商分组
 * */
export function listSupplierGroups() {
  return request({
    url: '/api/v1/kingdee/suppliergroups',
    method: 'post'
  })
}

/**
 * 获取图片预览地址
 * @param {String} imageUrl 图片地址
 * @returns
 */
export function getImagePreviewUrl(imageUrl) {
  if (!imageUrl) return '';
  // 如果是完整的URL，直接返回
  if (imageUrl.startsWith('http') || imageUrl.startsWith('https')) {
    return imageUrl;
  }
  // 否则认为是相对路径，拼接基础URL
  return `${import.meta.env.VITE_APP_BASE_API || ''}${imageUrl}`;
}

/**
 * 查询供应商修改审计日志（分页）
 * @param supplierId 供应商ID
 * @param pageNum 页码，默认1
 * @param pageSize 每页大小，默认10
 * @returns 审计日志分页数据
 */
export function getSupplierAuditLogs(supplierId, pageNum = 1, pageSize = 10) {
  return request({
    url: '/k3/supplier/auditLogs',
    method: 'get',
    params: {
      supplierId,
      pageNum,
      pageSize
    }
  })
}
