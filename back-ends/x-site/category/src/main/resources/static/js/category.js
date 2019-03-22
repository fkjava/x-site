var addHoverDom = function (treeId, treeNode) {
    // 找到节点的span
    var sObj = $("#" + treeNode.tId + "_span");
    //
    if (treeNode.editNameFlag // 判断是否正在编辑名字
        || $("#addBtn_" + treeNode.tId).length > 0// 判断是否有添加按钮
    ) {
        // 在编辑名字、有添加按钮都不需要再增加新的自定义按钮
        return;
    }
    // 自定义按钮，其实也是一个span
    var addStr = "<span class='button add' id='addBtn_" + treeNode.tId
        + "' title='添加菜单' onfocus='this.blur();'></span>";
    // 把自定义的按钮的HTML，放到节点的span之后
    sObj.after(addStr);
    // 给按钮绑定事件
    var btn = $("#addBtn_" + treeNode.tId);
    if (btn)
        btn.bind("click", function () {
            // 删除自定义按钮
            removeHoverDom(treeId, treeNode);
            // 找到已有的tree
            var zTree = $.fn.zTree.getZTreeObj(treeId);
            // treeNode : 添加新的节点到treeNode里面
            // id : 如果为null则自动增加一个、pId : 上级节点的id、name : 显示的名称
            var nodes = zTree.addNodes(treeNode, {
                name: "新菜单"
            });
            // 选中节点，因为只是添加了一个，所以nodes里面只有一个！
            zTree.selectNode(nodes[0], false, true);

            return false;
        });
};

var dropNode = function (event, treeId, treeNodes, targetNode, moveType) {
    var requestData = new Object();
    // 要移动的节点的id
    requestData.id = treeNodes[0].id;
    if (!requestData.id) {
        // 没有node的ID，表示新增的节点，不需要到服务器移动
        return;
    }
    // 要把节点移动到哪个目标位置
    if (targetNode) {
        requestData.targetId = targetNode.id;
    } else {
        // 没有目标节点
        requestData.targetId = "";
    }
    // inner、prev、next
    requestData.moveType = moveType;

    $.ajax({
        url: moveURL,
        method: "POST",
        dataType: "json",
        data: requestData,
        success: function (data, status, xhr) {
            // 正常的时候，什么都处理
        },
        error: function (data, status, xhr) {
            alert(data.responseJSON.message);
        }
    });
};

// 显示删除按钮
var showRemoveBtn = function (treeId, treeNode) {
    // 没有下级菜单，可以删除
    return treeNode.children == 0;
};

// 执行删除的操作
var removeNode = function (treeId, treeNode) {
    var zTree = $.fn.zTree.getZTreeObj(treeId);
    // false 表示不要触发回调
    zTree.removeNode(treeNode, false);
};

var beforeRemoveNode = function (treeId, treeNode) {

    // 无论如何，都返回false，避免zTree自己把节点删除
    // 应该通过AJAX把服务器里面的数据删除成功以后，手动调用API来删除节点！
    $.ajax({
        url: removeURL + "/" + treeNode.id,
        method: "DELETE",
        dataType: "json",
        success: function (data, status, xhr) {
            // == 两个等号会进行类型转换，比如 1 == "1" 返回true
            // === 三个等号不会进行类型转换，比如 1 === "1" 返回false
            // 在error回调中要加上responseJSON获取服务器返回的JSON
            // 在success回调，则不需要responseJSON
            if (data.code === 1) {
                // 删除成功，把节点从页面移除
                removeNode(treeId, treeNode);
            }
        },
        error: function (data, status, xhr) {
            alert(data.responseJSON.message);
        }
    });

    return false;
};

// 检查是否有同名的节点，在同一个节点下面不能有同名的子节点。
// 由于一级节点禁止重命名，所以treeNode总能获取父节点。
var beforeRenameNode = function (treeId, treeNode, newName, isCancel) {
    var nodes = treeNode.getParentNode().children;
    //console.log(nodes);

    // 检查nodes里面是否有重名的
    // 要求是：
    // 1.id不能跟treeNode的id相同
    // 2.如果name跟newName相同，则表示重名
    // 3.isCancel则不处理
    if (!isCancel) {
        var checked = nodes.every(function (value, index, array) {
            if (treeNode !== value && value.name === newName) {
                // ID不能、名称相同
                return false;
            }
        });
        //console.log("重名检查: " + checked);
        return !checked;
    }
    return false;
};

/**
 * 把重命名的操作更新到数据库
 * @param event 标准JS数据
 * @param treeId 树的ID
 * @param treeNode 被修改名称后的节点
 * @param isCancel 是否取消，如果为false才需要同步
 */
var onRename = function (event, treeId, treeNode, isCancel) {
    var c = {
        id: treeNode.id,
        name: treeNode.name,
        parent: {
            id: treeNode.getParentNode() ? treeNode.getParentNode().id : null
        }
    };
    $.ajax("/category", {
        data: JSON.stringify(c),
        method: "POST",
        contentType: "application/json",
        success: function (data, status, xhr) {
            alert(data.message);
        }
    });
};

var setting = {
    async: {
        // 激活异步请求
        enable: true,
        // 异步请求的URL，默认POST方式发送请求
        url: loadURL,
        // 使用GET方式发送请求
        type: "GET",
        // 要求返回JSON，数据类型参考jQuery的dataType
        dataType: "JSON"
    },
    view: {
        // 当鼠标移动在节点上的时候，增加自定义的按钮
        addHoverDom: addHoverDom,
        // 当鼠标离开节点的时候，删除自定义按钮
        removeHoverDom: removeHoverDom,
        // 禁止多选
        selectedMulti: false
    },
    edit: {
        enable: true,
        showRenameBtn: function (treeId, treeNode) {
            // 不是一级节点，均可重命名
            //console.log(treeNode);
            //console.log(treeNode.getParentNode());
            return treeNode.getParentNode() != null;
        },
        drag: {
            // 禁止复制
            isCopy: false,
            // 允许拖动
            isMove: true
        },
        showRemoveBtn: showRemoveBtn
    },
    callback: {
        beforeDrag: function(treeId, treeNode){
            return treeNode[0].getParentNode() != null;
        },
        onDrop: dropNode,
        beforeRemove: beforeRemoveNode,
        beforeRename: beforeRenameNode,
        // onRemove: removeNode
        onRename: onRename
    }
};

// 使用var声明的函数，系统在运行的时候会把var放到最上面，但是还未赋值
// 如果使用function直接声明函数，那么会在所有执行语句执行之前，先把函数分配空间
// 内存分配顺序： function -> var -> let
// var removeHoverDom = function(treeId, treeNode){
function removeHoverDom(treeId, treeNode) {
    // 删除自定义的按钮
    $("#addBtn_" + treeNode.tId).unbind().remove();
};

$(document).ready(function () {
    $.fn.zTree.init($("#treeDemo"), setting);
});