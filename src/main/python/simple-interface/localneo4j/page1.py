# 初始化一个 process 和 task
from py2neo import Graph, Node, Relationship, NodeMatcher

graph = Graph("bolt://192.168.1.123:7687", auth=("neo4j", "happy_cassini"))


# 查询所有的“ProcessInstance”节点

def get_simple_node(label: str, key: str, value: str):
    cypher = ("match(n:%s{%s:'%s'}) return n limit 1" % (label, key, value))
    print(cypher)
    result = graph.run(cypher)
    for r in result:
        print(r)
    return r


