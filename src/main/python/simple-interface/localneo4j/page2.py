# 初始化一个 process 和 task
from py2neo import Graph, Node, Relationship, NodeMatcher


graph = Graph("bolt://192.168.1.123:7687", auth=("neo4j", "happy_cassini"))


# 查询所有的“ProcessInstance”节点

def get_all_nodes_and_edges():
    result = graph.run(
        r'match(:ProcessInstance{name:"测试的任务"})-[:SUB_TASK]->(n:TaskInstance)-[r:NEXT]->(m:TaskInstance) RETURN n,r,m')

    node_dict = {}
    edges = []
    for x, y, z in result:
        start_name = x['name']
        x['id'] = start_name
        node_dict[start_name] = x

        end_name = z['name']
        z['id'] = end_name
        node_dict[end_name] = z

        data = {'from': start_name, 'label': 'NEXT', 'to': end_name}
        edges.append(data)

    nodes = [{'id': v['id'], 'label': v['name'], 'command': v['command'], 'color': v['color']} for k, v in
             node_dict.items()]
    for n in nodes:
        if n['id'] == 'start':
            n['fixed'] = True

    return nodes, edges


get_all_nodes_and_edges()
