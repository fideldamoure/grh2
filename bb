// نسخة DartPad: ملف واحد، تخزين في الذاكرة فقط.
import 'dart:convert';
import 'package:flutter/material.dart';
void main() => runApp(const App());
class App extends StatelessWidget {
  const App({super.key});
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'الرقابة القبلية على مسابقات التوظيف',
      debugShowCheckedModeBanner: false,
      builder: (context, child) =>
          Directionality(textDirection: TextDirection.rtl, child: child!),
      theme: ThemeData(
        useMaterial3: true,
        colorSchemeSeed: const Color(0xFF1B5E20),
      ),
      home: const Scaffold(body: Center(child: Text('مرحباً بكم'))),
    );
  }
}
/// وصف حقول ملف المسابقة. يُولَّد منه النموذج تلقائيا،
/// وتُقرأ منه تسميات الحقول في تقرير الرقابة.
enum FieldType { text, number, date, flag, choice, multi }
class FieldSpec {
  final String key;
  final String label;
  final FieldType type;
  final Map<String, String> options;
  final String? help;

  const FieldSpec(
    this.key,
    this.label,
    this.type, {
    this.options = const {},
    this.help,
  });
}
class FormSection {
  final String title;
  final List<FieldSpec> fields;
  const FormSection(this.title, this.fields);
}
final List<FormSection> formSections = [
  const FormSection('هوية المسابقة', [
    FieldSpec('commune', 'البلدية', FieldType.text),
    FieldSpec('daira', 'الدائرة', FieldType.text),
    FieldSpec('reference', 'مرجع الإعلان / المداولة', FieldType.text),
    FieldSpec('operationType', 'نوع العملية', FieldType.choice, options: {
      'contract_2026': 'توظيف أعوان متعاقدين بالبلديات (تعليمة 10 أوت 2026)',
      'general': 'مسابقة توظيف أخرى',
    }),
    FieldSpec('recruitmentMode', 'أسلوب التوظيف', FieldType.choice, options: {
      'diploma': 'على أساس الشهادة',
      'tests': 'على أساس الاختبارات',
      'professional_test': 'اختبار مهني (أعوان متعاقدون)',
    }),
    FieldSpec('postTitle', 'المنصب / الرتبة', FieldType.text),
    FieldSpec('postsCount', 'عدد المناصب المفتوحة', FieldType.number),
    FieldSpec('authorizedPosts', 'عدد المناصب المالية المرخص بها',
        FieldType.number),
  ]),
  const FormSection('الإجراءات القبلية', [
    FieldSpec('deliberationDate', 'تاريخ مداولة المجلس الشعبي البلدي',
        FieldType.date),
    FieldSpec('dairaApprovalDate', 'تاريخ مصادقة الدائرة', FieldType.date),
    FieldSpec('wilayaApprovalDate', 'تاريخ مصادقة الولاية', FieldType.date),
    FieldSpec('budgetCreditsAllocated',
        'تم تخصيص الاعتمادات المالية اللازمة للرواتب', FieldType.flag),
    FieldSpec('inspectorateNotified',
        'تم التنسيق مع مفتشية الوظيفة العمومية', FieldType.flag),
  ]),
  const FormSection('الإعلان عن المسابقة', [
    FieldSpec('announcementStart', 'تاريخ أول إعلان', FieldType.date),
    FieldSpec('announcementEnd', 'تاريخ نهاية الإعلان / إيداع الملفات',
        FieldType.date),
    FieldSpec('announcementChannels', 'قنوات الإعلان', FieldType.multi,
        options: {
          'anem_wilaya': 'الوكالة الولائية للتشغيل',
          'anem_local': 'الوكالة المحلية للتشغيل',
          'commune_hq': 'مقر البلدية',
          'other': 'مكان آخر مناسب',
        }),
    FieldSpec('announcementHasPostLocation',
        'الإعلان يحدد مكان التعيين بدقة (مدرسة / مطعم مدرسي...)',
        FieldType.flag),
    FieldSpec('announcementHasConditions',
        'الإعلان يتضمن شروط الالتحاق (الجنسية، السن، المؤهل...)',
        FieldType.flag),
    FieldSpec('announcementHasFileContents',
        'الإعلان يتضمن مكونات ملف الترشح', FieldType.flag),
    FieldSpec('announcementHasDeadlineAndVenue',
        'الإعلان يتضمن أجل ومكان إيداع الملفات', FieldType.flag),
    FieldSpec('residenceCriterion',
        'الإعلان يشترط الإقامة بالبلدية', FieldType.flag),
    FieldSpec('contractFullTime', 'التعاقد بالتوقيت الكامل', FieldType.flag),
  ]),
  const FormSection('الانتقاء والاختبارات والنتائج', [
    FieldSpec('selectionCommitteeDecisionDate',
        'تاريخ مقرر تشكيل لجنة الانتقاء / المسابقة', FieldType.date),
    FieldSpec('examDate', 'تاريخ الاختبار', FieldType.date),
    FieldSpec('resultsPostedDate', 'تاريخ نشر النتائج', FieldType.date),
    FieldSpec('appealsHandled', 'تمت معالجة الطعون', FieldType.flag),
  ]),
  const FormSection('التنصيب', [
    FieldSpec('installationDate', 'تاريخ تنصيب الناجحين', FieldType.date),
    FieldSpec('hrSystemEntered',
        'أُدرجت بيانات المنصّبين في النظام المعلوماتي لتسيير الموارد البشرية',
        FieldType.flag),
  ]),
];

final Map<String, String> _labels = {
  for (final s in formSections)
    for (final f in s.fields) f.key: f.label,
};

String labelOf(String key) => _labels[key] ?? key;



enum Severity { blocking, warning, info }

enum Outcome { pass, fail, missing, notApplicable }

enum Verdict { compliant, reservations, rejected }

String severityLabel(Severity s) {
  switch (s) {
    case Severity.blocking:
      return 'جوهري (مانع)';
    case Severity.warning:
      return 'تحفّظ';
    case Severity.info:
      return 'للعلم';
  }
}

String verdictLabel(Verdict v) {
  switch (v) {
    case Verdict.compliant:
      return 'مطابق';
    case Verdict.reservations:
      return 'مطابق مع تحفظات / بيانات ناقصة';
    case Verdict.rejected:
      return 'غير مطابق — يُوقف الإجراء إلى حين التصحيح';
  }
}

/// قاعدة قانونية واحدة. تُقرأ من ملف JSON حتى يمكن تحديث النصوص
/// القانونية دون تعديل الشيفرة.
class Rule {
  final String id;
  final String title;
  final Severity severity;
  final String legalRef;
  final String advice;
  final bool verified;
  final Map<String, List<String>> appliesWhen;
  final Map<String, dynamic> check;

  Rule({
    required this.id,
    required this.title,
    required this.severity,
    required this.legalRef,
    required this.advice,
    required this.verified,
    required this.appliesWhen,
    required this.check,
  });

  factory Rule.fromJson(Map<String, dynamic> j) {
    final when = <String, List<String>>{};
    final raw = j['appliesWhen'];
    if (raw is Map) {
      raw.forEach((k, v) {
        when[k.toString()] = (v as List).map((e) => e.toString()).toList();
      });
    }
    return Rule(
      id: j['id'] as String,
      title: j['title'] as String,
      severity: Severity.values.byName((j['severity'] ?? 'warning') as String),
      legalRef: (j['legalRef'] ?? '') as String,
      advice: (j['advice'] ?? '') as String,
      verified: (j['verified'] ?? false) as bool,
      appliesWhen: when,
      check: Map<String, dynamic>.from(j['check'] as Map),
    );
  }
}

/// يقبل إما كائنا {"rules": [...]} أو قائمة مباشرة.
List<Rule> parseRules(String jsonText) {
  final decoded = jsonDecode(jsonText);
  final list = decoded is Map ? decoded['rules'] : decoded;
  return (list as List)
      .map((e) => Rule.fromJson(Map<String, dynamic>.from(e as Map)))
      .toList();
}

class RuleResult {
  final Rule rule;
  final Outcome outcome;
  final String detail;
  const RuleResult(this.rule, this.outcome, this.detail);
}

// ---------------------------------------------------------------- helpers

bool _has(Map<String, dynamic> f, String k) {
  final v = f[k];
  if (v == null) return false;
  if (v is String && v.trim().isEmpty) return false;
  if (v is List && v.isEmpty) return false;
  return true;
}

DateTime? _d(Map<String, dynamic> f, String k) {
  final v = f[k];
  if (v is! String || v.isEmpty) return null;
  final p = DateTime.tryParse(v);
  if (p == null) return null;
  return DateTime.utc(p.year, p.month, p.day);
}

String _fmt(DateTime d) => d.toIso8601String().substring(0, 10);

List<String> _strs(dynamic v) => (v as List).map((e) => e.toString()).toList();

// ------------------------------------------------------------- evaluation

RuleResult evaluateRule(Rule r, Map<String, dynamic> f) {
  for (final e in r.appliesWhen.entries) {
    final v = f[e.key]?.toString();
    if (v == null || !e.value.contains(v)) {
      return RuleResult(r, Outcome.notApplicable, 'لا ينطبق على هذه المسابقة');
    }
  }

  RuleResult pass(String m) => RuleResult(r, Outcome.pass, m);
  RuleResult fail(String m) => RuleResult(r, Outcome.fail, m);
  RuleResult missing(String m) => RuleResult(r, Outcome.missing, m);

  final c = r.check;
  final type = c['type'];

  if (type == 'required') {
    final absent =
        _strs(c['fields']).where((k) => !_has(f, k)).map(labelOf).toList();
    return absent.isEmpty
        ? pass('جميع البيانات متوفرة')
        : fail('غير مدرج: ${absent.join('، ')}');
  }

  if (type == 'date_chain') {
    final fields = _strs(c['fields']);
    final ds = <DateTime>[];
    final miss = <String>[];
    for (final k in fields) {
      final d = _d(f, k);
      if (d == null) {
        miss.add(labelOf(k));
      } else {
        ds.add(d);
      }
    }
    if (miss.isNotEmpty) return missing('بيانات ناقصة: ${miss.join('، ')}');
    for (var i = 0; i < ds.length - 1; i++) {
      if (ds[i + 1].isBefore(ds[i])) {
        return fail('«${labelOf(fields[i + 1])}» (${_fmt(ds[i + 1])}) '
            'سابق لـ«${labelOf(fields[i])}» (${_fmt(ds[i])})');
      }
    }
    return pass('الترتيب الزمني سليم');
  }

  if (type == 'min_days_between' || type == 'max_days_between') {
    final fromKey = c['from'] as String;
    final toKey = c['to'] as String;
    final from = _d(f, fromKey);
    final to = _d(f, toKey);
    if (from == null || to == null) {
      return missing(
          'بيانات ناقصة: ${labelOf(fromKey)} / ${labelOf(toKey)}');
    }
    if (type == 'min_days_between') {
      final inclusive = c['inclusive'] != false;
      final days = to.difference(from).inDays + (inclusive ? 1 : 0);
      final min = (c['min'] as num).toInt();
      return days >= min
          ? pass('المدة $days يوما (الحد الأدنى $min)')
          : fail('المدة $days يوما فقط، والحد الأدنى $min يوما');
    } else {
      final days = to.difference(from).inDays;
      final max = (c['max'] as num).toInt();
      return days <= max
          ? pass('المدة $days يوما (الحد الأقصى $max)')
          : fail('المدة $days يوما، والحد الأقصى $max يوما');
    }
  }

  if (type == 'on_or_before') {
    final key = c['field'] as String;
    final d = _d(f, key);
    if (d == null) return missing('بيانات ناقصة: ${labelOf(key)}');
    final limit = DateTime.parse(c['limit'] as String);
    final lim = DateTime.utc(limit.year, limit.month, limit.day);
    return !d.isAfter(lim)
        ? pass('${_fmt(d)} ضمن الأجل (${_fmt(lim)})')
        : fail('${_fmt(d)} بعد الأجل المحدد (${_fmt(lim)})');
  }

  if (type == 'flag_true') {
    final key = c['field'] as String;
    return f[key] == true ? pass('مستوفى') : fail('غير مستوفى');
  }

  if (type == 'flag_false') {
    final key = c['field'] as String;
    return f[key] != true ? pass('غير وارد') : fail('وارد في الإعلان');
  }

  if (type == 'all_true') {
    final bad =
        _strs(c['fields']).where((k) => f[k] != true).map(labelOf).toList();
    return bad.isEmpty ? pass('مستوفى') : fail('غير مستوفى: ${bad.join('، ')}');
  }

  if (type == 'contains_all') {
    final key = c['field'] as String;
    final have = f[key] is List ? _strs(f[key]) : <String>[];
    final need = _strs(c['values']);
    final lacking = need.where((v) => !have.contains(v)).toList();
    if (lacking.isEmpty) return pass('جميع القنوات المطلوبة متوفرة');
    final names = <String>[];
    for (final s in formSections) {
      for (final fs in s.fields) {
        if (fs.key == key) {
          for (final v in lacking) {
            names.add(fs.options[v] ?? v);
          }
        }
      }
    }
    return fail('قنوات غير مستوفاة: ${names.join('، ')}');
  }

  if (type == 'number_lte') {
    final a = f[c['field']];
    final b = f[c['other']];
    if (a is! num || b is! num) {
      return missing('بيانات ناقصة: ${labelOf(c['field'] as String)} / '
          '${labelOf(c['other'] as String)}');
    }
    return a <= b
        ? pass('$a ≤ $b')
        : fail('${labelOf(c['field'] as String)} ($a) يتجاوز '
            '${labelOf(c['other'] as String)} ($b)');
  }

  return missing('نوع فحص غير معروف: $type');
}

List<RuleResult> evaluateAll(List<Rule> rules, Map<String, dynamic> facts) =>
    rules.map((r) => evaluateRule(r, facts)).toList();

class ControlReport {
  final List<RuleResult> results;
  const ControlReport(this.results);

  Verdict get verdict {
    final blockingFail = results.any(
        (x) => x.outcome == Outcome.fail && x.rule.severity == Severity.blocking);
    if (blockingFail) return Verdict.rejected;
    final soft = results.any((x) =>
        (x.outcome == Outcome.fail && x.rule.severity == Severity.warning) ||
        (x.outcome == Outcome.missing && x.rule.severity != Severity.info));
    return soft ? Verdict.reservations : Verdict.compliant;
  }

  String toText(String title) {
    final b = StringBuffer()
      ..writeln('تقرير الرقابة القبلية — $title')
      ..writeln('النتيجة: ${verdictLabel(verdict)}')
      ..writeln(
          'ملاحظة: نتيجة آلية مساعدة، لا تغني عن رأي الجهة المختصة ولا عن مراجعة النصوص الأصلية.')
      ..writeln();
    for (final x in results) {
      if (x.outcome == Outcome.pass || x.outcome == Outcome.notApplicable) {
        continue;
      }
      final tag = x.outcome == Outcome.missing ? 'بيانات ناقصة' : 'مخالفة';
      b
        ..writeln('[$tag · ${severityLabel(x.rule.severity)}] ${x.rule.title}')
        ..writeln('  التفصيل: ${x.detail}')
        ..writeln('  السند: ${x.rule.legalRef}')
        ..writeln(
            x.rule.advice.isEmpty ? '' : '  التوصية: ${x.rule.advice}');
    }
    final ok = results.where((x) => x.outcome == Outcome.pass).length;
    b.writeln('عدد القواعد المستوفاة: $ok');
    return b.toString();
  }
}