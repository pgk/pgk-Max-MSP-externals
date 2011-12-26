inlets = jsarguments.length - 1 || 1;
outlets = 1;

var PrependMultiple = {
	prepends: Array.prototype.splice.call(jsarguments,1),
	scheme: function (f) {
		return function(inp) { return f(inp, PrependMultiple.prepends); };
	}
};

var Generator = PrependMultiple.scheme(function (inl, prep) { return prep[inl]});

function anything(val) {
	outlet(0, [Generator(inlet)].concat(arrayfromargs(messagename, arguments)));
}