import os
import yaml

def load_yaml(path):
    with open(path, "r", encoding="utf-8") as f:
        return yaml.safe_load(f)

def extract_entries(changelog_yaml):
    """Return the ordered list of entries inside databaseChangeLog."""
    return changelog_yaml.get("databaseChangeLog", [])

def resolve_path(root_dir, changelog_dir, path):
    """Resolve SQL or include paths relative to the root directory."""
    if os.path.isabs(path):
        return path
    return os.path.join(root_dir, path)

def process_changelog(root_dir, changelog_path, combined_sql):
    """Recursively process a changelog file and append SQL contents."""
    changelog_yaml = load_yaml(changelog_path)
    entries = extract_entries(changelog_yaml)

    for entry in entries:

        # Case 1: include another changelog
        if "include" in entry:
            include_file = entry["include"]["file"]
            nested_path = resolve_path(root_dir, os.path.dirname(changelog_path), include_file)

            if not os.path.exists(nested_path):
                raise FileNotFoundError(f"Included changelog not found: {nested_path}")

            process_changelog(root_dir, nested_path, combined_sql)

        # Case 2: changeSet containing sqlFile entries
        if "changeSet" in entry:
            cs = entry["changeSet"]

            for change in cs.get("changes", []):
                if "sqlFile" in change:
                    sql_path = change["sqlFile"]["path"]
                    full_sql_path = resolve_path(root_dir, os.path.dirname(changelog_path), sql_path)

                    if not os.path.exists(full_sql_path):
                        raise FileNotFoundError(f"SQL file not found: {full_sql_path}")

                    with open(full_sql_path, "r", encoding="utf-8") as f:
                        combined_sql.append(f"-- Start: {sql_path}\n")
                        combined_sql.append(f.read())
                        combined_sql.append(f"\n-- End: {sql_path}\n\n")

def compile_all_sql(root_dir):
    master_path = os.path.join(root_dir, "db.changelog-master.yaml")

    if not os.path.exists(master_path):
        raise FileNotFoundError(f"Master changelog not found: {master_path}")

    combined_sql = []
    process_changelog(root_dir, master_path, combined_sql)
    return "".join(combined_sql)

if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(description="Compile SQL from Liquibase changelogs.")
    parser.add_argument("directory", help="Root directory containing db.changelog-master.yaml")
    parser.add_argument("-o", "--output", default="combined.sql", help="Output file")

    args = parser.parse_args()

    output = compile_all_sql(args.directory)

    with open(args.output, "w", encoding="utf-8") as f:
        f.write(output)

    print(f"Combined SQL written to {args.output}")
