"""
Enhanced Product Ingest with Unified Transaction Support
Atomic ingestion with rollback (optimized order: external storage first, then database)
"""
import logging
from typing import List, Dict
from fastapi import UploadFile, HTTPException

from services.transaction_service import IngestTransaction
from dependencies import (
    get_clip_service,
    get_rembg_service,
    get_image_processor
)

logger = logging.getLogger(__name__)


async def ingest_product_with_transaction(
    product_code: str,
    name: str,
    spec: str = None,
    category: str = None,
    files: List[UploadFile] = None,
    remove_bg: bool = False
) -> Dict:
    """
    Ingest product with transaction support (atomic + rollback)
    
    Args:
        product_code: Product code
        name: Product name
        spec: Product specification
        category: Product category
        files: List of image files
        remove_bg: Whether to remove background
    
    Returns:
        {
            "success": bool,
            "product_code": str,
            "ingested_images": int,
            "milvus_ids": List[int],
            "minio_paths": List[str]
        }
    """
    if not files:
        raise HTTPException(status_code=400, detail="At least one image file is required")
    
    # Get services
    clip_service = get_clip_service()
    rembg_service = get_rembg_service()
    image_processor = get_image_processor()
    
    # Create transaction manager (use simple mode without compensation)
    transaction = IngestTransaction(product_code, use_compensation=False)
    
    try:
        # Phase 1: Prepare all images
        logger.info(f"Phase 1: Preparing {len(files)} images for {product_code}")
        prepared_images = []
        
        for idx, file in enumerate(files):
            try:
                # Read image bytes
                image_bytes = await file.read()
                
                # Remove background if requested
                if remove_bg and rembg_service:
                    try:
                        image_bytes = rembg_service.remove_background(image_bytes)
                        logger.debug(f"Background removed for image {idx+1}")
                    except Exception as e:
                        logger.warning(f"Background removal failed for image {idx+1}: {str(e)}")
                
                # Extract features ONCE
                processed_image = image_processor.preprocess(image_bytes)
                embedding = clip_service.extract_features(processed_image)
                
                # Compute hash
                image_hash = image_processor.compute_hash(image_bytes)
                
                # Check for duplicates
                from dependencies import get_product_service
                product_service = get_product_service()
                if product_service.image_exists(image_hash):
                    logger.warning(f"Duplicate image detected: {file.filename}")
                    continue
                
                # Save to temp location (pass pre-extracted embedding and hash to avoid re-computation)
                prepared = transaction.prepare_image(
                    image_bytes, 
                    file.filename, 
                    embedding=embedding,  # Pass pre-extracted embedding
                    image_hash=image_hash  # Pass pre-computed hash
                )
                prepared_images.append(prepared)
                
                logger.debug(f"Image {idx+1}/{len(files)} prepared: {file.filename}")
                
            except Exception as e:
                logger.error(f"Failed to prepare image {idx+1}: {str(e)}")
                raise
        
        if not prepared_images:
            raise HTTPException(status_code=400, detail="No valid images to ingest (all may be duplicates)")
        
        # Phase 2: Execute atomic ingestion
        logger.info(f"Phase 2: Executing atomic ingestion for {len(prepared_images)} images")
        
        product_info = {
            "code": product_code,
            "name": name,
            "spec": spec,
            "category": category
        }
        
        result = transaction.execute_ingest(prepared_images, product_info)
        
        logger.info(f" Product {product_code} ingested successfully: {result['ingested_images']} images")
        
        return {
            "success": True,
            "product_code": product_code,
            "ingested_images": result["ingested_images"],
            "milvus_ids": result["milvus_ids"],
            "minio_paths": result["minio_paths"]
        }
        
    except Exception as e:
        logger.error(f"💥 Transaction failed for {product_code}: {str(e)}", exc_info=True)
        raise
