package cn.juns.summer.db.condition;

import java.util.List;

public class Nodes extends Node {
    private Node left;
    private PathType pathType;
    private Node right;

    public Nodes(Node left, PathType pathType, Node right) {
        this.left = left;
        this.pathType = pathType;
        this.right = right;
    }

    public void toSqlString(String alias, StringBuilder sb, List<Object> params) {
        sb.append("(");
        this.left.toSqlString(alias, sb, params);
        sb.append(" ").append(pathType.name()).append(" ");
        this.right.toSqlString(alias, sb, params);
        sb.append(")");
    }

    public Node getLeft() {
        return left;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public PathType getPathType() {
        return pathType;
    }

    public void setPathType(PathType pathType) {
        this.pathType = pathType;
    }

    public Node getRight() {
        return right;
    }

    public void setRight(Node right) {
        this.right = right;
    }
}
