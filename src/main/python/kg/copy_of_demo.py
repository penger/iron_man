import typing

import pandas as pd
from py2neo import Graph, Node, Subgraph, NodeMatcher, Relationship, RelationshipMatcher

'''加载数据'''


def load_data() -> typing.Dict[str, pd.DataFrame]:
    df_actor_movie = pd.read_csv('./dataset/movie_act.csv')
    df_actor = pd.read_csv('./dataset/movie_actor.csv')
    df_movie = pd.read_csv('./dataset/movie_movie.csv')
    df_popularity = pd.read_csv('./dataset/movie_popularity.csv')
    df_user = pd.read_csv('./dataset/user_user.csv')

    return {
        'actor_movie': df_actor_movie,
        'actor': df_actor,
        'movie': df_movie,
        'popularity': df_popularity,
        'user': df_user
    }


df = load_data()

print(type(df['actor_movie']))

print('rows:', len(df['actor_movie']))
print(df['actor_movie'])

dict_movie = {}
dict_actor = {}
dict_actor_movie = {}

# 最受欢迎电影的id
list_popularity_movie = list(df['popularity']['movieid_id'])
print('受欢迎的共有：', len(list_popularity_movie))

# 最受欢迎电影的列表
for i in range(len(df['movie'])):
    row = df['movie'].iloc[i]
    if row['movieid'] in list_popularity_movie:
        dict_movie.update({row['movieid']: row.to_dict()})

# 受欢迎电影的所有演员
filter = df['actor_movie']['movieid_id'].isin(list_popularity_movie)
print(filter)
list_popularity_actor = list(df['actor_movie']['actorid_id'][filter])

# 演员转换成字典
for i in range(len(df['actor'])):
    row = df['actor'].iloc[i]
    if row['actorid'] in list_popularity_actor:
        dict_actor.update({row['actorid']: row.to_dict()})

# 演员-> 电影转换成字典
for i in range(len(df['actor_movie'])):
    row = df['actor_movie'].iloc[i]
    dict_actor_movie.update({row['actorid_id']: row.to_dict()})

url = "http://192.168.1.123"
username = 'neo4j'
password = 'happy_cassini'

graph = Graph(url, auth=(username, password), name='neo4j')
graph.delete_all()

nodes = []

for mid, node_movie in dict_movie.items():
    nodes.append(Node('movie', **node_movie))

for aid, node_actor in dict_actor.items():
    nodes.append(Node('actor', **node_actor))

batch_size = 50

for i in range(0, len(nodes), batch_size):
    graph.create(Subgraph(nodes=nodes[i:i + batch_size]))

rels = []

nodeMatcher = NodeMatcher(graph)
for aid, node_actor_movie in dict_actor_movie.items():
    mid = node_actor_movie['movieid_id']
    node_movie = nodeMatcher.match('movie', movieid=mid).first()
    node_actor = nodeMatcher.match('actor', actorid=aid).first()
    if node_movie is not None and node_actor is not None:
        rels.append(Relationship(node_actor, 'acted', node_movie, name="acted"))

for i in range(0,len(rels), batch_size):
    graph.create(Subgraph(relationships=rels[i:i+batch_size]))

rm = RelationshipMatcher(graph)
print('查询 movie 电影节点的统计结果: ', nodeMatcher.match('movie').count())
print('查询 actor 演员节点的统计结果: ', nodeMatcher.match('actor').count())
print('查询 acted 扮演关系的统计结果: ', rm.match(name='acted').count())