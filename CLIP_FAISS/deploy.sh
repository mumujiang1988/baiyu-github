#!/bin/bash

# ========================================
# 企业产品以图搜系统 - 部署脚本
# ========================================

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查Docker
check_docker() {
    log_info "检查Docker环境..."
    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装，请先安装Docker"
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose未安装，请先安装Docker Compose"
        exit 1
    fi

    log_info "Docker环境检查通过"
}

# 创建必要的目录
create_directories() {
    log_info "创建必要的目录..."
    mkdir -p backend/uploads
    mkdir -p backend/logs
    mkdir -p monitoring/grafana/dashboards
    log_info "目录创建完成"
}

# 构建镜像
build_images() {
    log_info "构建Docker镜像..."
    docker-compose build --no-cache
    log_info "镜像构建完成"
}

# 启动服务
start_services() {
    log_info "启动服务..."
    docker-compose up -d
    log_info "服务启动完成"
}

# 检查服务状态
check_status() {
    log_info "检查服务状态..."
    docker-compose ps
}

# 等待服务就绪
wait_for_services() {
    log_info "等待服务就绪..."
    sleep 10

    # 检查后端健康
    for i in {1..30}; do
        if curl -f http://localhost:8000/api/health &> /dev/null; then
            log_info "后端服务就绪"
            break
        fi
        log_warn "等待后端服务... ($i/30)"
        sleep 2
    done
}

# 显示访问信息
show_info() {
    echo ""
    log_info "=========================================="
    log_info "部署完成！"
    log_info "=========================================="
    echo ""
    log_info "访问地址："
    log_info "  前端: http://localhost"
    log_info "  后端API: http://localhost:8000"
    log_info "  API文档: http://localhost:8000/docs"
    log_info "  Grafana: http://localhost:3000 (admin/admin123456)"
    echo ""
    log_info "常用命令："
    log_info "  查看日志: docker-compose logs -f"
    log_info "  停止服务: docker-compose down"
    log_info "  重启服务: docker-compose restart"
    log_info "  查看状态: docker-compose ps"
    echo ""
}

# 主函数
main() {
    log_info "开始部署企业产品以图搜系统..."
    echo ""

    check_docker
    create_directories
    build_images
    start_services
    wait_for_services
    check_status
    show_info
}

# 停止服务
stop() {
    log_info "停止服务..."
    docker-compose down
    log_info "服务已停止"
}

# 重启服务
restart() {
    log_info "重启服务..."
    docker-compose restart
    log_info "服务已重启"
}

# 查看日志
logs() {
    docker-compose logs -f
}

# 清理
clean() {
    log_warn "清理所有容器、镜像和数据卷..."
    docker-compose down -v --rmi-all
    log_info "清理完成"
}

# 帮助信息
help() {
    echo "使用方法: $0 [命令]"
    echo ""
    echo "可用命令:"
    echo "  deploy   - 部署系统（默认）"
    echo "  stop     - 停止服务"
    echo "  restart  - 重启服务"
    echo "  logs     - 查看日志"
    echo "  status   - 查看状态"
    echo "  clean    - 清理所有"
    echo "  help     - 显示帮助"
}

# 处理命令
case "${1:-deploy}" in
    deploy)
        main
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    logs)
        logs
        ;;
    status)
        check_status
        ;;
    clean)
        clean
        ;;
    help)
        help
        ;;
    *)
        log_error "未知命令: $1"
        help
        exit 1
        ;;
esac
