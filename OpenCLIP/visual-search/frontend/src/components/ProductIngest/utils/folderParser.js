/**
 * 文件夹解析工具
 */

/**
 * 解析文件路径，提取产品文件夹名称
 * @param {File} file - 文件对象
 * @param {string} structure - 目录结构类型 ('standard' | 'scene')
 * @param {string} sceneNames - 场景文件夹名称（逗号分隔）
 * @returns {string|null} 产品文件夹名称
 */
export function extractFolderName(file, structure, sceneNames = '') {
  const pathParts = file.webkitRelativePath.split('/')
  
  let folderName = ''
  
  if (structure === 'standard') {
    // 标准模式：父/产品/图片
    if (pathParts.length >= 3) {
      folderName = pathParts[1]
    } else if (pathParts.length === 2) {
      folderName = pathParts[0]
    }
  } else if (structure === 'scene') {
    // 场景模式：父/产品/场景/图片
    if (pathParts.length >= 4) {
      folderName = pathParts[1]
    } else if (pathParts.length === 3) {
      folderName = pathParts[0]
    }
    
    // 如果配置了场景文件夹名称，需要验证
    if (sceneNames.trim()) {
      const configuredScenes = sceneNames.split(',').map(s => s.trim()).filter(s => s)
      if (configuredScenes.length > 0) {
        const sceneFolder = pathParts[pathParts.length - 2]
        if (!configuredScenes.includes(sceneFolder)) {
          return null
        }
      }
    }
  }
  
  return folderName || null
}

/**
 * 解析产品文件夹名称为产品信息
 * @param {string} folderName - 文件夹名称（格式：编码_名称_规格_分类）
 * @returns {Object} 产品信息
 */
export function parseProductInfo(folderName) {
  const parts = folderName.split('_')
  return {
    productCode: parts[0]?.trim() || folderName,
    productName: parts[1]?.trim() || (parts[0]?.trim() || folderName),
    spec: parts[2]?.trim() || '',
    category: parts[3]?.trim() || ''
  }
}

/**
 * 按文件夹分组文件
 * @param {FileList} files - 文件列表
 * @param {string} structure - 目录结构类型
 * @param {string} sceneNames - 场景文件夹名称
 * @returns {Map<string, File[]>} 文件夹名称 -> 文件数组的映射
 */
export function groupFilesByFolder(files, structure, sceneNames = '') {
  const folderMap = new Map()
  
  for (const file of files) {
    // 只处理图片文件
    if (!file.type.startsWith('image/')) {
      continue
    }
    
    const folderName = extractFolderName(file, structure, sceneNames)
    if (!folderName) {
      continue
    }
    
    if (!folderMap.has(folderName)) {
      folderMap.set(folderName, [])
    }
    
    folderMap.get(folderName).push(file)
  }
  
  return folderMap
}
