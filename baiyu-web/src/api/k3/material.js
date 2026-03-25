import request from '@/utils/request'

// 查询岗位列表
export function listMaterial(query) {
  return request({
    url: '/k3/material/page',
    method: 'get',
    params: query
  })
}

//选择物料分组
export function dictionaryLookupTreeSelect() {
  return request({
    url: '/api/v1/kingdee/dictionaryLookup',
    method: 'post'
  })
}

//物料属性
export function materialDictionary(categoryName){
  return request({
    url: '/api/v1/kingdee/MaterialDictionary',
    method: 'get',
    params: { categoryName }
  })
}

//新老产品
export function xlproductTree(categoryName) {
  return request({
    url: '/api/v1/kingdee/xlproduct',
    method: 'get',
    params: { categoryName }
  })
}

//产品类别
export function getProductCategory(categoryName) {
  return request({
    url: '/api/v1/kingdee/ProductCategory',
    method: 'get',
    params: { categoryName }
  })
}

export function addMaterial(data,imageFile = null, inspectionReportFile = null){

  const formData = new FormData();

  // 1. 将物料数据转为 Blob 并添加到 FormData
  const materialBlob = new Blob([JSON.stringify(data)], {
    type: 'application/json'
  });

  formData.append('bymaterial', materialBlob);

  // 2. 添加图片文件（如果存在）
  if (imageFile) {
    formData.append('image', imageFile);
  }

  // 3. 添加检测报告文件（如果存在）
  if (inspectionReportFile) {
    formData.append('inspectionReport', inspectionReportFile);
  }

  return request({
    url: '/k3/material/add',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}


export function updateMaterial(data, imageFile = null, inspectionReportFile = null) {

  const formData = new FormData();

  // 1. 将物料数据转为 Blob 并添加到 FormData
  const materialBlob = new Blob([JSON.stringify(data)], {
    type: 'application/json'
  });

  formData.append('material', materialBlob);

  // 2. 添加图片文件（如果存在）
  if (imageFile) {
    formData.append('image', imageFile);
  }

  // 3. 添加检测报告文件（如果存在）
  if (inspectionReportFile) {
    formData.append('inspectionReport', inspectionReportFile);
  }

  return request({
    url: '/k3/material/update',
    method: 'put',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/* AI填充物料英文描述 */
export function handleAIFillEnglish(formData){
  return request({
    url: '/k3/material/fillEnglishDesc',
    method: 'post',
    data: formData
  })
}


/**
 * 根据编码查询物料
 */
export function getByNumber(id) {
  return request({
    url: '/k3/material/getByNumber',
    method: 'get',
    params: { id }
  })
}

/**
 * 批量更新交付红线
* */

export function updateMaterials(ids,state){

  const formData = new FormData();

  const idArray = Array.isArray(ids) ? ids : [ids];

  idArray.forEach(id => {
    formData.append('ids', String(id));  // 转换为字符串  // 注意：后端期望的是 Long[]，这里每个 id 作为单独的字段
  });
  formData.append('state', String(state));  // 转换为字符串

  return request({
    url: '/k3/material/updateMaterials',
    method: 'put',
    data: formData
  })
}


// 批量删除物料
export function removeBatch(ids) {
  const idArray = Array.isArray(ids) ? ids : [ids];
  return request({
    url: '/k3/material/removeBatch',
    method: 'post',
    data: idArray

  })
}

/**
 * 查询物料修改审计日志（分页）
 * @param materialId 物料ID
 * @param pageNum 页码，默认1
 * @param pageSize 每页大小，默认10
 * @returns 审计日志分页数据
 */
export function getMaterialAuditLogs(materialId, pageNum = 1, pageSize = 10) {
  return request({
    url: '/k3/material/auditLogs',
    method: 'get',
    params: { 
      materialId,
      pageNum,
      pageSize
    }
  })
}
