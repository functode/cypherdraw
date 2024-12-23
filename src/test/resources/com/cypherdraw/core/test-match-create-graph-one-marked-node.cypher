MATCH
(`x1`:A)
CREATE
(`x2`:B),
(`x1`)-[:R]->(`x2`)
RETURN *
