MATCH
(`x1`:A),
(`x2`:B {a: 1}),
(`x1`)-[:R {a: 1}]->(`x2`)
RETURN *
