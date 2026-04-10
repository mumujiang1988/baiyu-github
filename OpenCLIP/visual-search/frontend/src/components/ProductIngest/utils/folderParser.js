/**
 * 文件夹解析工具
 */
import { logger } from '../../../utils/logger'

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
    // 标准模式：父/产品/图片 或 产品/图片
    if (pathParts.length >= 3) {
      // 有父目录：父/产品/图片 -> 取产品名（索引1）
      folderName = pathParts[1]
    } else if (pathParts.length === 2) {
      // 无父目录：产品/图片 -> 取产品名（索引0）
      folderName = pathParts[0]
    }
  } else if (structure === 'scene') {
    // 场景模式：父/产品/场景/图片 或 产品/场景/图片
    if (pathParts.length >= 4) {
      // 有父目录：父/产品/场景/图片 -> 取产品名（索引1）
      folderName = pathParts[1]
    } else if (pathParts.length === 3) {
      // 无父目录：产品/场景/图片 -> 取产品名（索引0）
      folderName = pathParts[0]
    }
    
    // 如果配置了场景文件夹名称，需要验证当前文件的场景文件夹是否在配置列表中
    if (sceneNames.trim() && pathParts.length >= 3) {
      const configuredScenes = sceneNames.split(',').map(s => s.trim()).filter(s => s)
      if (configuredScenes.length > 0) {
        // 场景文件夹是倒数第二个路径部分
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
  
  logger.log('[FolderParser] 开始分组文件', {
    totalFiles: files.length,
    structure,
    sceneNames
  })
  
  let skippedNonImage = 0
  let skippedNoFolder = 0
  
  for (const file of files) {
    // 只处理图片文件
    if (!file.type.startsWith('image/')) {
      skippedNonImage++
      continue
    }
    
    const folderName = extractFolderName(file, structure, sceneNames)
    if (!folderName) {
      skippedNoFolder++
      logger.log('[FolderParser] 跳过文件（无法提取文件夹名）', {
        path: file.webkitRelativePath,
        structure,
        pathParts: file.webkitRelativePath.split('/')
      })
      continue
    }
    
    if (!folderMap.has(folderName)) {
      folderMap.set(folderName, [])
    }
    
    folderMap.get(folderName).push(file)
  }
  
  logger.log('[FolderParser] 分组完成', {
    folderCount: folderMap.size,
    skippedNonImage,
    skippedNoFolder,
    folders: Array.from(folderMap.entries()).map(([name, files]) => ({
      name,
      fileCount: files.length
    }))
  })
  
  return folderMap
}
